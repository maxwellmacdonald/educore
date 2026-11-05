import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CalendarCheck, GraduationCap, Mail, Pencil, Trash2, User } from 'lucide-react';
import { attendanceApi, errorMessage, studentsApi } from '../api/client.js';
import { isSample, useAsync } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { Avatar, ConfirmDialog, ErrorState, SampleBadge, Skeleton } from '../components/ui.jsx';

function Info({ icon: Icon, label, value }) {
  return (
    <div className="flex items-start gap-3 rounded-xl bg-slate-50 p-4">
      <Icon className="mt-0.5 h-5 w-5 text-brand-500" />
      <div>
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p>
        <p className="mt-0.5 text-sm font-medium text-slate-800">{value}</p>
      </div>
    </div>
  );
}

export default function StudentDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const student = useAsync(() => studentsApi.get(id), [id]);
  const stats = useAsync(() => attendanceApi.stats({ studentId: id }), [id]);

  async function remove() {
    setDeleting(true);
    try {
      await studentsApi.remove(id);
      toast.success('Student deleted');
      navigate('/students');
    } catch (err) {
      toast.error(errorMessage(err));
      setDeleting(false);
    }
  }

  const s = student.data;
  return (
    <>
      <Link to="/students" className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-800">
        <ArrowLeft className="h-4 w-4" /> All students
      </Link>

      {student.error ? (
        <div className="card"><ErrorState message={student.error} onRetry={student.reload} /></div>
      ) : student.loading || !s ? (
        <div className="grid gap-6 lg:grid-cols-3"><Skeleton className="h-64 lg:col-span-2" /><Skeleton className="h-64" /></div>
      ) : (
        <div className="grid animate-fade-up gap-6 lg:grid-cols-3">
          <div className="card p-6 sm:p-8 lg:col-span-2">
            <div className="flex flex-wrap items-center gap-4">
              <Avatar name={s.fullName} size="h-16 w-16 text-lg" />
              <div className="min-w-0 flex-1">
                <h1 className="text-2xl font-bold tracking-tight text-navy-900">{s.fullName}{isSample(s.admissionNumber) && <SampleBadge />}</h1>
                <p className="font-mono text-sm text-slate-500">{s.admissionNumber}</p>
              </div>
              <div className="flex gap-2">
                <Link to={`/students/${id}/edit`} className="btn btn-ghost"><Pencil className="h-4 w-4" /> Edit</Link>
                <button className="btn btn-ghost !text-rose-600 hover:!bg-rose-50" onClick={() => setConfirming(true)}><Trash2 className="h-4 w-4" /> Delete</button>
              </div>
            </div>
            <div className="mt-8 grid gap-3 sm:grid-cols-2">
              <Info icon={GraduationCap} label="Class" value={s.className} />
              <Info icon={Mail} label="Email" value={s.email} />
              <Info icon={User} label="Gender / Age" value={`${s.gender}, ${s.age} years`} />
              <Info icon={CalendarCheck} label="Registered" value={s.registrationDate} />
            </div>
          </div>

          <div className="card p-6">
            <h2 className="text-base font-semibold text-navy-900">Attendance record</h2>
            {stats.loading ? <Skeleton className="mt-4 h-32" /> : stats.error ? (
              <p className="mt-3 text-sm text-rose-600">{stats.error}</p>
            ) : stats.data.total === 0 ? (
              <p className="mt-3 text-sm text-slate-500">No attendance has been recorded for this student yet.</p>
            ) : (
              <>
                <p className="mt-4 text-4xl font-bold tracking-tight text-navy-900">{stats.data.percentage}%</p>
                <p className="text-xs text-slate-500">attended across {stats.data.total} recorded days</p>
                <div className="mt-5 h-2 overflow-hidden rounded-full bg-slate-100">
                  <div className="h-full rounded-full bg-brand-500 transition-all duration-700" style={{ width: `${stats.data.percentage}%` }} />
                </div>
                <div className="mt-5 grid grid-cols-3 gap-2 text-center text-xs">
                  <div className="rounded-lg bg-emerald-50 p-2 text-emerald-700"><b className="block text-base">{stats.data.present}</b>Present</div>
                  <div className="rounded-lg bg-amber-50 p-2 text-amber-700"><b className="block text-base">{stats.data.late}</b>Late</div>
                  <div className="rounded-lg bg-rose-50 p-2 text-rose-700"><b className="block text-base">{stats.data.absent}</b>Absent</div>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {confirming && (
        <ConfirmDialog
          title="Delete student?"
          message={`This permanently removes ${s?.fullName} and all of their attendance records. This cannot be undone.`}
          busy={deleting}
          onConfirm={remove}
          onCancel={() => setConfirming(false)}
        />
      )}
    </>
  );
}
