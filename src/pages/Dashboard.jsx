import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { ArrowUpRight, BookOpen, CalendarCheck, CalendarClock, School, UserPlus, Users } from 'lucide-react';
import { dashboardApi } from '../api/client.js';
import { isSample, todayStr, useAsync } from '../hooks.js';
import { Avatar, EmptyState, ErrorState, PageHeader, SampleBadge, Skeleton } from '../components/ui.jsx';

function Metric({ icon: Icon, label, value, hint, tone, delay }) {
  return (
    <div className="card animate-fade-up p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-lift" style={{ animationDelay: `${delay}ms` }}>
      <div className={`mb-4 flex h-11 w-11 items-center justify-center rounded-xl ${tone}`}>
        <Icon className="h-5 w-5" />
      </div>
      <p className="text-3xl font-bold tracking-tight text-navy-900">{value}</p>
      <p className="mt-1 text-sm font-medium text-slate-500">{label}</p>
      {hint && <p className="mt-0.5 text-xs text-slate-400">{hint}</p>}
    </div>
  );
}

const quickActions = [
  { to: '/students/new', label: 'Add student', icon: UserPlus },
  { to: '/attendance', label: 'Take attendance', icon: CalendarCheck },
  { to: '/classes', label: 'Manage classes', icon: School },
  { to: '/subjects', label: 'Manage subjects', icon: BookOpen },
];

export default function Dashboard() {
  const [date, setDate] = useState(todayStr());
  const { data, loading, error, reload } = useAsync(() => dashboardApi.summary({ date }), [date]);

  return (
    <>
      <PageHeader
        title="Overview"
        subtitle="Live figures from your school database."
        actions={
          <label className="flex items-center gap-2 text-sm text-slate-500">
            <CalendarClock className="h-4 w-4" />
            <input type="date" className="input !w-auto !py-2" value={date} max={todayStr()} onChange={(e) => e.target.value && setDate(e.target.value)} />
          </label>
        }
      />

      {error ? (
        <div className="card"><ErrorState message={error} onRetry={reload} /></div>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            {loading || !data ? (
              Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-36 !rounded-2xl" />)
            ) : (
              <>
                <Metric delay={0} icon={Users} label="Total students" value={data.totalStudents} tone="bg-brand-50 text-brand-600" />
                <Metric delay={60} icon={School} label="Classes" value={data.totalClasses} tone="bg-violet-50 text-violet-600" />
                <Metric delay={120} icon={BookOpen} label="Subjects" value={data.totalSubjects} tone="bg-amber-50 text-amber-600" />
                <Metric
                  delay={180}
                  icon={CalendarCheck}
                  label={`Attendance on ${data.date}`}
                  value={`${data.attendancePercentage}%`}
                  hint={`${data.presentCount} present · ${data.absentCount} absent · ${data.lateCount} late`}
                  tone="bg-emerald-50 text-emerald-600"
                />
              </>
            )}
          </div>

          <div className="mt-6 grid gap-6 lg:grid-cols-2">
            <div className="card p-6">
              <h2 className="text-base font-semibold text-navy-900">Student enrollment by class</h2>
              <p className="mb-4 text-xs text-slate-500">Enrolled students compared with class capacity</p>
              {loading || !data ? <Skeleton className="h-64" /> : data.enrollmentByClass.length === 0 ? (
                <EmptyState icon={School} title="No classes yet" message="Create a class to see enrollment." action={<Link to="/classes" className="btn btn-primary">Add a class</Link>} />
              ) : (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data.enrollmentByClass} margin={{ left: -20 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis dataKey="className" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                      <YAxis allowDecimals={false} tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                      <Tooltip cursor={{ fill: '#f1f5f9' }} />
                      <Legend iconType="circle" wrapperStyle={{ fontSize: 12 }} />
                      <Bar dataKey="capacity" name="Capacity" fill="#dbe7ff" radius={[6, 6, 0, 0]} />
                      <Bar dataKey="students" name="Students" fill="#2f6bff" radius={[6, 6, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>

            <div className="card p-6">
              <h2 className="text-base font-semibold text-navy-900">Attendance, last 7 days</h2>
              <p className="mb-4 text-xs text-slate-500">Percentage attended (present + late) per day</p>
              {loading || !data ? <Skeleton className="h-64" /> : (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={data.attendanceTrend.map((p) => ({ ...p, label: p.date.slice(5) }))} margin={{ left: -20 }}>
                      <defs>
                        <linearGradient id="attFill" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#2f6bff" stopOpacity={0.35} />
                          <stop offset="100%" stopColor="#2f6bff" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis dataKey="label" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                      <YAxis domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
                      <Tooltip formatter={(v) => [`${v}%`, 'Attendance']} />
                      <Area type="monotone" dataKey="percentage" stroke="#2f6bff" strokeWidth={2.5} fill="url(#attFill)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>
          </div>

          <div className="mt-6 grid gap-6 lg:grid-cols-3">
            <div className="card lg:col-span-2">
              <div className="flex items-center justify-between p-6 pb-3">
                <h2 className="text-base font-semibold text-navy-900">Recent registrations</h2>
                <Link to="/students" className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:text-brand-700">
                  View all <ArrowUpRight className="h-4 w-4" />
                </Link>
              </div>
              {loading || !data ? (
                <div className="space-y-3 p-6 pt-2">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
              ) : data.recentStudents.length === 0 ? (
                <EmptyState icon={Users} title="No students yet" message="Registered students will appear here." action={<Link to="/students/new" className="btn btn-primary">Add the first student</Link>} />
              ) : (
                <ul className="divide-y divide-slate-100 px-3 pb-3">
                  {data.recentStudents.map((s) => (
                    <li key={s.id}>
                      <Link to={`/students/${s.id}`} className="flex items-center gap-3 rounded-xl px-3 py-3 transition hover:bg-slate-50">
                        <Avatar name={s.fullName} />
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-semibold text-slate-800">{s.fullName}{isSample(s.admissionNumber) && <SampleBadge />}</p>
                          <p className="truncate text-xs text-slate-500">{s.admissionNumber} · {s.className}</p>
                        </div>
                        <span className="text-xs text-slate-400">{s.registrationDate}</span>
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="card p-6">
              <h2 className="mb-4 text-base font-semibold text-navy-900">Quick actions</h2>
              <div className="grid gap-2.5">
                {quickActions.map(({ to, label, icon: Icon }) => (
                  <Link key={to} to={to} className="group flex items-center gap-3 rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-700 transition hover:border-brand-200 hover:bg-brand-50 hover:text-brand-700">
                    <Icon className="h-5 w-5 text-slate-400 transition group-hover:text-brand-600" />
                    {label}
                    <ArrowUpRight className="ml-auto h-4 w-4 opacity-0 transition group-hover:opacity-100" />
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}
