import { useState } from 'react';
import { Loader2, Pencil, Plus, School, Trash2 } from 'lucide-react';
import { classesApi, errorMessage, fieldErrors } from '../api/client.js';
import { useAsync } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { ConfirmDialog, EmptyState, ErrorState, Field, Modal, PageHeader, TableSkeleton } from '../components/ui.jsx';

const blank = { name: '', level: '', classTeacher: '', capacity: '' };

function ClassModal({ initial, onClose, onSaved }) {
  const toast = useToast();
  const editing = Boolean(initial?.id);
  const [values, setValues] = useState(initial ? { ...initial, capacity: String(initial.capacity) } : blank);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const set = (f) => (e) => { setValues((v) => ({ ...v, [f]: e.target.value })); setErrors((x) => ({ ...x, [f]: undefined })); };

  async function submit(e) {
    e.preventDefault();
    const found = {};
    if (!values.name.trim()) found.name = 'Class name is required';
    if (!values.level.trim()) found.level = 'Level is required';
    if (!values.classTeacher.trim()) found.classTeacher = 'Class teacher is required';
    const cap = Number(values.capacity);
    if (values.capacity === '' || !Number.isInteger(cap) || cap < 1 || cap > 200) found.capacity = 'Capacity must be a whole number from 1 to 200';
    setErrors(found);
    if (Object.keys(found).length) return;

    setSaving(true);
    try {
      const body = { name: values.name, level: values.level, classTeacher: values.classTeacher, capacity: cap };
      if (editing) await classesApi.update(initial.id, body); else await classesApi.create(body);
      toast.success(editing ? 'Class updated' : 'Class created');
      onSaved();
    } catch (err) {
      const fe = fieldErrors(err);
      if (Object.keys(fe).length) setErrors(fe);
      else if (err.response?.status === 409) setErrors({ name: errorMessage(err) });
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  const cls = (f) => `input ${errors[f] ? 'input-error' : ''}`;
  return (
    <Modal title={editing ? 'Edit class' : 'Add class'} onClose={onClose}>
      <form onSubmit={submit} noValidate className="grid gap-4">
        <Field label="Class name" error={errors.name}><input className={cls('name')} value={values.name} onChange={set('name')} placeholder="e.g. Grade 7A" autoFocus /></Field>
        <Field label="Level / grade" error={errors.level}><input className={cls('level')} value={values.level} onChange={set('level')} placeholder="e.g. Grade 7" /></Field>
        <Field label="Class teacher" error={errors.classTeacher}><input className={cls('classTeacher')} value={values.classTeacher} onChange={set('classTeacher')} placeholder="e.g. Mrs. Adebayo" /></Field>
        <Field label="Capacity" error={errors.capacity}><input type="number" min="1" max="200" className={cls('capacity')} value={values.capacity} onChange={set('capacity')} placeholder="e.g. 30" /></Field>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" disabled={saving}>{saving && <Loader2 className="h-4 w-4 animate-spin" />}{editing ? 'Save changes' : 'Add class'}</button>
        </div>
      </form>
    </Modal>
  );
}

export default function Classes() {
  const toast = useToast();
  const { data, loading, error, reload } = useAsync(() => classesApi.list(), []);
  const [modal, setModal] = useState(null); // null | { initial }
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  async function confirmDelete() {
    setDeleting(true);
    try {
      await classesApi.remove(toDelete.id);
      toast.success(`${toDelete.name} was deleted`);
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <PageHeader title="Classes" subtitle="Organise students into classes and assign class teachers."
        actions={<button className="btn btn-primary" onClick={() => setModal({ initial: null })}><Plus className="h-4 w-4" /> Add class</button>} />
      <div className="card overflow-hidden">
        {error ? <ErrorState message={error} onRetry={reload} /> : loading ? <TableSkeleton rows={5} cols={4} /> : data.length === 0 ? (
          <EmptyState icon={School} title="No classes yet" message="Create your first class to start enrolling students."
            action={<button className="btn btn-primary" onClick={() => setModal({ initial: null })}><Plus className="h-4 w-4" /> Add class</button>} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[600px]">
              <thead className="bg-slate-50/70"><tr><th className="th">Class</th><th className="th">Level</th><th className="th">Class teacher</th><th className="th">Capacity</th><th className="th text-right">Actions</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((c) => (
                  <tr key={c.id} className="transition hover:bg-slate-50/70">
                    <td className="td font-semibold text-slate-800">{c.name}</td>
                    <td className="td text-slate-600">{c.level}</td>
                    <td className="td text-slate-600">{c.classTeacher}</td>
                    <td className="td text-slate-600">{c.capacity}</td>
                    <td className="td"><div className="flex justify-end gap-1">
                      <button className="icon-btn" title="Edit" onClick={() => setModal({ initial: c })}><Pencil className="h-4 w-4" /></button>
                      <button className="icon-btn hover:!bg-rose-50 hover:!text-rose-600" title="Delete" onClick={() => setToDelete(c)}><Trash2 className="h-4 w-4" /></button>
                    </div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {modal && <ClassModal initial={modal.initial} onClose={() => setModal(null)} onSaved={() => { setModal(null); reload(); }} />}
      {toDelete && (
        <ConfirmDialog title="Delete class?" busy={deleting} onConfirm={confirmDelete} onCancel={() => setToDelete(null)}
          message={`Delete ${toDelete.name}? Students already assigned to this class keep their class name, so reassign them separately.`} />
      )}
    </>
  );
}
