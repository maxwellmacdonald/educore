import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Eye, Pencil, Plus, Search, Trash2, Users } from 'lucide-react';
import { classesApi, errorMessage, studentsApi } from '../api/client.js';
import { isSample, useAsync, useDebounced } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { Avatar, ConfirmDialog, EmptyState, ErrorState, PageHeader, SampleBadge, TableSkeleton } from '../components/ui.jsx';

const PAGE_SIZE = 8;

export default function Students() {
  const navigate = useNavigate();
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [className, setClassName] = useState('');
  const [page, setPage] = useState(0);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const debouncedSearch = useDebounced(search);

  const classes = useAsync(() => classesApi.list(), []);
  const { data, loading, error, reload } = useAsync(
    () => studentsApi.list({ search: debouncedSearch || undefined, className: className || undefined, page, size: PAGE_SIZE }),
    [debouncedSearch, className, page]
  );

  const filtered = Boolean(debouncedSearch || className);

  async function confirmDelete() {
    setDeleting(true);
    try {
      await studentsApi.remove(toDelete.id);
      toast.success(`${toDelete.fullName} was deleted`);
      setToDelete(null);
      if (data.content.length === 1 && page > 0) setPage(page - 1);
      else reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Students"
        subtitle="Search, filter and manage every student record."
        actions={<Link to="/students/new" className="btn btn-primary"><Plus className="h-4 w-4" /> Add student</Link>}
      />

      <div className="card overflow-hidden">
        <div className="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input className="input !pl-10" placeholder="Search by name or admission number…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} />
          </div>
          <select className="input sm:!w-52" value={className} onChange={(e) => { setClassName(e.target.value); setPage(0); }}>
            <option value="">All classes</option>
            {(classes.data || []).map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
          </select>
        </div>

        {error ? (
          <ErrorState message={error} onRetry={reload} />
        ) : loading && !data ? (
          <TableSkeleton rows={PAGE_SIZE} cols={5} />
        ) : data.content.length === 0 ? (
          <EmptyState
            icon={Users}
            title={filtered ? 'No matching students' : 'No students yet'}
            message={filtered ? 'Try a different search or clear the class filter.' : 'Add your first student to get started.'}
            action={!filtered && <Link to="/students/new" className="btn btn-primary"><Plus className="h-4 w-4" /> Add student</Link>}
          />
        ) : (
          <div className={`overflow-x-auto transition-opacity ${loading ? 'opacity-60' : ''}`}>
            <table className="w-full min-w-[720px]">
              <thead className="bg-slate-50/70">
                <tr>
                  <th className="th">Student</th><th className="th">Admission no.</th><th className="th">Class</th>
                  <th className="th">Gender / Age</th><th className="th">Registered</th><th className="th text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((s) => (
                  <tr key={s.id} className="transition hover:bg-slate-50/70">
                    <td className="td">
                      <div className="flex items-center gap-3">
                        <Avatar name={s.fullName} />
                        <div>
                          <button className="text-left text-sm font-semibold text-slate-800 hover:text-brand-600" onClick={() => navigate(`/students/${s.id}`)}>{s.fullName}</button>
                          <p className="text-xs text-slate-500">{s.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="td font-mono text-xs text-slate-600">{s.admissionNumber}{isSample(s.admissionNumber) && <SampleBadge />}</td>
                    <td className="td"><span className="rounded-lg bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700">{s.className}</span></td>
                    <td className="td text-slate-600">{s.gender}, {s.age}</td>
                    <td className="td text-slate-500">{s.registrationDate}</td>
                    <td className="td">
                      <div className="flex justify-end gap-1">
                        <Link className="icon-btn" to={`/students/${s.id}`} title="View"><Eye className="h-4 w-4" /></Link>
                        <Link className="icon-btn" to={`/students/${s.id}/edit`} title="Edit"><Pencil className="h-4 w-4" /></Link>
                        <button className="icon-btn hover:!bg-rose-50 hover:!text-rose-600" title="Delete" onClick={() => setToDelete(s)}><Trash2 className="h-4 w-4" /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {data && data.totalElements > 0 && (
          <div className="flex items-center justify-between border-t border-slate-100 px-5 py-3 text-sm text-slate-500">
            <span>{data.totalElements} student{data.totalElements === 1 ? '' : 's'} · page {data.page + 1} of {Math.max(data.totalPages, 1)}</span>
            <div className="flex gap-1">
              <button className="icon-btn disabled:opacity-40" disabled={page === 0} onClick={() => setPage(page - 1)} aria-label="Previous page"><ChevronLeft className="h-4 w-4" /></button>
              <button className="icon-btn disabled:opacity-40" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} aria-label="Next page"><ChevronRight className="h-4 w-4" /></button>
            </div>
          </div>
        )}
      </div>

      {toDelete && (
        <ConfirmDialog
          title="Delete student?"
          message={`This permanently removes ${toDelete.fullName} (${toDelete.admissionNumber}) and all of their attendance records. This cannot be undone.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}
