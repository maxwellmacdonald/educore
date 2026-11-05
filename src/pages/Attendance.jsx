import { useEffect, useState } from 'react';
import { CalendarCheck, CheckCheck, Loader2, Save, Users } from 'lucide-react';
import { attendanceApi, classesApi, errorMessage, studentsApi } from '../api/client.js';
import { isSample, todayStr, useAsync } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { Avatar, EmptyState, ErrorState, PageHeader, SampleBadge, TableSkeleton } from '../components/ui.jsx';
import { Link } from 'react-router-dom';

const options = [
  { value: 'PRESENT', label: 'Present', on: 'bg-emerald-500 text-white border-emerald-500', off: 'hover:border-emerald-300 hover:text-emerald-700' },
  { value: 'LATE', label: 'Late', on: 'bg-amber-500 text-white border-amber-500', off: 'hover:border-amber-300 hover:text-amber-700' },
  { value: 'ABSENT', label: 'Absent', on: 'bg-rose-500 text-white border-rose-500', off: 'hover:border-rose-300 hover:text-rose-700' },
];

export default function Attendance() {
  const toast = useToast();
  const [date, setDate] = useState(todayStr());
  const [className, setClassName] = useState('');
  const [marks, setMarks] = useState({});
  const [saving, setSaving] = useState(false);

  const classes = useAsync(() => classesApi.list(), []);
  useEffect(() => {
    if (!className && classes.data?.length) setClassName(classes.data[0].name);
  }, [classes.data, className]);

  const roster = useAsync(
    () =>
      className
        ? Promise.all([
            studentsApi.list({ className, page: 0, size: 200 }),
            attendanceApi.list({ date, className }),
            attendanceApi.stats({ date, className }),
          ]).then(([students, records, stats]) => ({ students: students.content, records, stats }))
        : Promise.resolve({ students: [], records: [], stats: null }),
    [date, className]
  );

  useEffect(() => {
    if (roster.data) {
      const m = {};
      roster.data.records.forEach((r) => { m[r.studentId] = r.status; });
      setMarks(m);
    }
  }, [roster.data]);

  const students = roster.data?.students || [];
  const markedCount = students.filter((s) => marks[s.id]).length;

  async function save() {
    const entries = students.filter((s) => marks[s.id]).map((s) => ({ studentId: s.id, status: marks[s.id] }));
    if (!entries.length) return toast.error('Mark at least one student before saving');
    setSaving(true);
    try {
      await attendanceApi.saveBulk({ date, entries });
      toast.success(`Attendance saved for ${entries.length} student${entries.length === 1 ? '' : 's'}`);
      roster.reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  const stats = roster.data?.stats;
  return (
    <>
      <PageHeader title="Attendance" subtitle="Mark students present, late or absent for a class and date."
        actions={
          <>
            <input type="date" className="input !w-auto" value={date} max={todayStr()} onChange={(e) => e.target.value && setDate(e.target.value)} />
            <select className="input !w-auto" value={className} onChange={(e) => setClassName(e.target.value)}>
              {!className && <option value="">Select class…</option>}
              {(classes.data || []).map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
            </select>
          </>
        } />

      {stats && stats.total > 0 && (
        <div className="mb-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {[['Attendance', `${stats.percentage}%`, 'text-brand-600'], ['Present', stats.present, 'text-emerald-600'], ['Late', stats.late, 'text-amber-600'], ['Absent', stats.absent, 'text-rose-600']].map(([l, v, c]) => (
            <div key={l} className="card px-5 py-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{l}</p><p className={`mt-1 text-2xl font-bold ${c}`}>{v}</p></div>
          ))}
        </div>
      )}

      <div className="card overflow-hidden">
        {classes.error ? <ErrorState message={classes.error} onRetry={classes.reload} />
          : classes.loading ? <TableSkeleton rows={6} cols={3} />
          : !classes.data?.length ? (
            <EmptyState icon={CalendarCheck} title="Create a class first" message="Attendance is taken per class." action={<Link to="/classes" className="btn btn-primary">Go to classes</Link>} />
          ) : roster.error ? <ErrorState message={roster.error} onRetry={roster.reload} />
          : roster.loading && !roster.data ? <TableSkeleton rows={6} cols={3} />
          : students.length === 0 ? (
            <EmptyState icon={Users} title={`No students in ${className}`} message="Add students to this class to take attendance." action={<Link to="/students/new" className="btn btn-primary">Add student</Link>} />
          ) : (
            <>
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-5 py-3">
                <p className="text-sm text-slate-500">{markedCount} of {students.length} marked for {date}</p>
                <button className="btn btn-ghost !py-1.5" onClick={() => setMarks(Object.fromEntries(students.map((s) => [s.id, 'PRESENT'])))}>
                  <CheckCheck className="h-4 w-4" /> Mark all present
                </button>
              </div>
              <ul className={`divide-y divide-slate-100 transition-opacity ${roster.loading ? 'opacity-60' : ''}`}>
                {students.map((s) => (
                  <li key={s.id} className="flex flex-wrap items-center gap-3 px-5 py-3.5">
                    <Avatar name={s.fullName} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-slate-800">{s.fullName}{isSample(s.admissionNumber) && <SampleBadge />}</p>
                      <p className="font-mono text-xs text-slate-500">{s.admissionNumber}</p>
                    </div>
                    <div className="flex gap-1.5">
                      {options.map((o) => (
                        <button key={o.value} onClick={() => setMarks((m) => ({ ...m, [s.id]: o.value }))}
                          className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition ${marks[s.id] === o.value ? o.on : `border-slate-200 bg-white text-slate-500 ${o.off}`}`}>
                          {o.label}
                        </button>
                      ))}
                    </div>
                  </li>
                ))}
              </ul>
              <div className="flex justify-end border-t border-slate-100 p-4">
                <button className="btn btn-primary" onClick={save} disabled={saving || markedCount === 0}>
                  {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />} Save attendance
                </button>
              </div>
            </>
          )}
      </div>
    </>
  );
}
