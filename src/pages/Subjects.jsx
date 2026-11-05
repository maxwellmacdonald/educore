import { useState } from 'react';
import { BookOpen, Loader2, Pencil, Plus, Trash2 } from 'lucide-react';
import { classesApi, errorMessage, fieldErrors, subjectsApi } from '../api/client.js';
import { useAsync } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { ConfirmDialog, EmptyState, ErrorState, Field, Modal, PageHeader, TableSkeleton } from '../components/ui.jsx';

const blank = { name: '', code: '', className: '', teacherName: '' };

function SubjectModal({ initial, classNames, onClose, onSaved }) {
  const toast = useToast();
  const editing = Boolean(initial?.id);
  const [values, setValues] = useState(initial || blank);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const set = (f) => (e) => { setValues((v) => ({ ...v, [f]: e.target.value })); setErrors((x) => ({ ...x, [f]: undefined })); };
  const options = [...classNames];
  if (values.className && !options.includes(values.className)) options.push(values.className);

  async function submit(e) {
    e.preventDefault();
    const found = {};
    if (!values.name.trim()) found.name = 'Subject name is required';
    if (!values.code.trim()) found.code = 'Subject code is required';
    if (!values.className) found.className = 'Select a class';
    if (!values.teacherName.trim()) found.teacherName = 'Teacher name is required';
    setErrors(found);
    if (Object.keys(found).length) return;

    setSaving(true);
    try {
      const body = { name: values.name, code: values.code, className: values.className, teacherName: values.teacherName };
      if (editing) await subjectsApi.update(initial.id, body); else await subjectsApi.create(body);
      toast.success(editing ? 'Subject updated' : 'Subject created');
      onSaved();
    } catch (err) {
      const fe = fieldErrors(err);
      if (Object.keys(fe).length) setErrors(fe);
      else if (err.response?.status === 409) setErrors({ code: errorMessage(err) });
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  const cls = (f) => `input ${errors[f] ? 'input-error' : ''}`;
  return (
    <Modal title={editing ? 'Edit subject' : 'Add subject'} onClose={onClose}>
      <form onSubmit={submit} noValidate className="grid gap-4">
        <Field label="Subject name" error={errors.name}><input className={cls('name')} value={values.name} onChange={set('name')} placeholder="e.g. Mathematics" autoFocus /></Field>
        <Field label="Subject code" error={errors.code}><input className={cls('code')} value={values.code} onChange={set('code')} placeholder="e.g. MTH7" /></Field>
        <Field label="Class" error={errors.className}>
          <select className={cls('className')} value={values.className} onChange={set('className')}>
            <option value="">Select…</option>{options.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Field>
        <Field label="Teacher" error={errors.teacherName}><input className={cls('teacherName')} value={values.teacherName} onChange={set('teacherName')} placeholder="e.g. Mr. Lawal" /></Field>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" disabled={saving}>{saving && <Loader2 className="h-4 w-4 animate-spin" />}{editing ? 'Save changes' : 'Add subject'}</button>
        </div>
      </form>
    </Modal>
  );
}

export default function Subjects() {
  const toast = useToast();
  const [className, setClassName] = useState('');
  const classes = useAsync(() => classesApi.list(), []);
  const { data, loading, error, reload } = useAsync(() => subjectsApi.list({ className: className || undefined }), [className]);
  const [modal, setModal] = useState(null);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const classNames = (classes.data || []).map((c) => c.name);

  async function confirmDelete() {
    setDeleting(true);
    try {
      await subjectsApi.remove(toDelete.id);
      toast.success(`${toDelete.name} was deleted`);
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  const addButton = <button className="btn btn-primary" onClick={() => setModal({ initial: null })}><Plus className="h-4 w-4" /> Add subject</button>;
  return (
    <>
      <PageHeader title="Subjects" subtitle="Manage the subjects taught in each class." actions={addButton} />
      <div className="card overflow-hidden">
        <div className="border-b border-slate-100 p-4">
          <select className="input sm:!w-56" value={className} onChange={(e) => setClassName(e.target.value)}>
            <option value="">All classes</option>{classNames.map((c) => <option key={c}>{c}</option>)}
          </select>
        </div>
        {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <TableSkeleton rows={5} cols={4} /> : data.length === 0 ? (
          <EmptyState icon={BookOpen} title={className ? `No subjects for ${className}` : 'No subjects yet'} message="Add a subject and assign it to a class and teacher." action={addButton} />
        ) : (
          <div className={`overflow-x-auto transition-opacity ${loading ? 'opacity-60' : ''}`}>
            <table className="w-full min-w-[620px]">
              <thead className="bg-slate-50/70"><tr><th className="th">Subject</th><th className="th">Code</th><th className="th">Class</th><th className="th">Teacher</th><th className="th text-right">Actions</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((s) => (
                  <tr key={s.id} className="transition hover:bg-slate-50/70">
                    <td className="td font-semibold text-slate-800">{s.name}</td>
                    <td className="td font-mono text-xs text-slate-600">{s.code}</td>
                    <td className="td"><span className="rounded-lg bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700">{s.className}</span></td>
                    <td className="td text-slate-600">{s.teacherName}</td>
                    <td className="td"><div className="flex justify-end gap-1">
                      <button className="icon-btn" title="Edit" onClick={() => setModal({ initial: s })}><Pencil className="h-4 w-4" /></button>
                      <button className="icon-btn hover:!bg-rose-50 hover:!text-rose-600" title="Delete" onClick={() => setToDelete(s)}><Trash2 className="h-4 w-4" /></button>
                    </div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {modal && <SubjectModal initial={modal.initial} classNames={classNames} onClose={() => setModal(null)} onSaved={() => { setModal(null); reload(); }} />}
      {toDelete && <ConfirmDialog title="Delete subject?" busy={deleting} onConfirm={confirmDelete} onCancel={() => setToDelete(null)} message={`Delete ${toDelete.name} (${toDelete.code})? This cannot be undone.`} />}
    </>
  );
}
