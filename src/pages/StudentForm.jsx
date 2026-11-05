import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Loader2, Save } from 'lucide-react';
import { classesApi, errorMessage, fieldErrors, studentsApi } from '../api/client.js';
import { todayStr, useAsync } from '../hooks.js';
import { useToast } from '../components/Toast.jsx';
import { ErrorState, Field, PageHeader, Skeleton } from '../components/ui.jsx';

const empty = { admissionNumber: '', fullName: '', email: '', gender: '', className: '', age: '', registrationDate: todayStr() };

function validate(v) {
  const e = {};
  if (!v.admissionNumber.trim()) e.admissionNumber = 'Admission number is required';
  if (!v.fullName.trim()) e.fullName = 'Full name is required';
  if (!v.email.trim()) e.email = 'Email is required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email.trim())) e.email = 'Enter a valid email address';
  if (!v.gender) e.gender = 'Select a gender';
  if (!v.className) e.className = 'Select a class';
  const age = Number(v.age);
  if (v.age === '') e.age = 'Age is required';
  else if (!Number.isInteger(age) || age < 3 || age > 25) e.age = 'Age must be a whole number between 3 and 25';
  return e;
}

export default function StudentForm() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const toast = useToast();
  const [values, setValues] = useState(empty);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const classes = useAsync(() => classesApi.list(), []);
  const student = useAsync(() => (editing ? studentsApi.get(id) : Promise.resolve(null)), [id]);

  useEffect(() => {
    const s = student.data;
    if (s) {
      setValues({
        admissionNumber: s.admissionNumber, fullName: s.fullName, email: s.email, gender: s.gender,
        className: s.className, age: String(s.age), registrationDate: s.registrationDate || todayStr(),
      });
    }
  }, [student.data]);

  const set = (field) => (e) => {
    setValues((v) => ({ ...v, [field]: e.target.value }));
    setErrors((er) => ({ ...er, [field]: undefined }));
  };

  async function submit(e) {
    e.preventDefault();
    const found = validate(values);
    setErrors(found);
    if (Object.keys(found).length) return;

    const body = { ...values, age: Number(values.age), registrationDate: values.registrationDate || null };
    setSaving(true);
    try {
      const saved = editing ? await studentsApi.update(id, body) : await studentsApi.create(body);
      toast.success(editing ? 'Student updated' : 'Student added');
      navigate(`/students/${saved.id}`);
    } catch (err) {
      const fe = fieldErrors(err);
      if (Object.keys(fe).length) setErrors(fe);
      else if (err.response?.status === 409) setErrors({ admissionNumber: errorMessage(err) });
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  const classOptions = [...(classes.data || [])].map((c) => c.name);
  if (values.className && !classOptions.includes(values.className)) classOptions.push(values.className);
  const cls = (f) => `input ${errors[f] ? 'input-error' : ''}`;

  return (
    <>
      <Link to={editing ? `/students/${id}` : '/students'} className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-800">
        <ArrowLeft className="h-4 w-4" /> Back
      </Link>
      <PageHeader title={editing ? 'Edit student' : 'Add student'} subtitle={editing ? 'Update the student’s record.' : 'Register a new student.'} />

      <div className="card max-w-3xl animate-fade-up p-6 sm:p-8">
        {student.error ? (
          <ErrorState message={student.error} onRetry={student.reload} />
        ) : editing && student.loading ? (
          <div className="grid gap-5 sm:grid-cols-2">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-16" />)}</div>
        ) : (
          <form onSubmit={submit} noValidate>
            {!classes.loading && !classes.error && classOptions.length === 0 && (
              <div className="mb-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                No classes exist yet. <Link className="font-semibold underline" to="/classes">Create a class</Link> first, then come back.
              </div>
            )}
            {classes.error && <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">{classes.error}</div>}

            <div className="grid gap-5 sm:grid-cols-2">
              <Field label="Admission number" error={errors.admissionNumber}>
                <input className={cls('admissionNumber')} value={values.admissionNumber} onChange={set('admissionNumber')} placeholder="e.g. ADM-2026-001" />
              </Field>
              <Field label="Full name" error={errors.fullName}>
                <input className={cls('fullName')} value={values.fullName} onChange={set('fullName')} placeholder="e.g. Ada Obi" />
              </Field>
              <Field label="Email" error={errors.email}>
                <input type="email" className={cls('email')} value={values.email} onChange={set('email')} placeholder="name@example.com" />
              </Field>
              <Field label="Gender" error={errors.gender}>
                <select className={cls('gender')} value={values.gender} onChange={set('gender')}>
                  <option value="">Select…</option><option>Female</option><option>Male</option><option>Other</option>
                </select>
              </Field>
              <Field label="Class" error={errors.className}>
                <select className={cls('className')} value={values.className} onChange={set('className')}>
                  <option value="">Select…</option>
                  {classOptions.map((c) => <option key={c}>{c}</option>)}
                </select>
              </Field>
              <Field label="Age" error={errors.age}>
                <input type="number" min="3" max="25" className={cls('age')} value={values.age} onChange={set('age')} placeholder="3 – 25" />
              </Field>
              <Field label="Registration date" error={errors.registrationDate}>
                <input type="date" className={cls('registrationDate')} value={values.registrationDate} onChange={set('registrationDate')} />
              </Field>
            </div>

            <div className="mt-8 flex justify-end gap-2">
              <Link to={editing ? `/students/${id}` : '/students'} className="btn btn-ghost">Cancel</Link>
              <button className="btn btn-primary" disabled={saving}>
                {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                {editing ? 'Save changes' : 'Add student'}
              </button>
            </div>
          </form>
        )}
      </div>
    </>
  );
}
