import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  BookOpen, CalendarCheck, GraduationCap, LayoutDashboard, Menu, PanelLeftClose, PanelLeftOpen, School, Users, X,
} from 'lucide-react';

const nav = [
  { to: '/', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/students', label: 'Students', icon: Users },
  { to: '/classes', label: 'Classes', icon: School },
  { to: '/subjects', label: 'Subjects', icon: BookOpen },
  { to: '/attendance', label: 'Attendance', icon: CalendarCheck },
];

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { pathname } = useLocation();
  const current = nav.find((n) => (n.end ? pathname === n.to : pathname.startsWith(n.to)));
  const hideLabel = collapsed ? 'lg:hidden' : '';

  return (
    <div className="min-h-screen">
      {mobileOpen && <div className="fixed inset-0 z-30 bg-navy-950/50 lg:hidden" onClick={() => setMobileOpen(false)} />}

      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col bg-gradient-to-b from-navy-900 to-navy-950 text-slate-300 transition-all duration-300 lg:translate-x-0 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        } ${collapsed ? 'lg:w-[76px]' : 'lg:w-64'}`}
      >
        <div className="flex h-16 items-center gap-3 px-5">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-brand-500 text-white shadow-lg shadow-brand-500/30">
            <GraduationCap className="h-5 w-5" />
          </div>
          <span className={`text-lg font-bold tracking-tight text-white ${hideLabel}`}>EduCore</span>
          <button className="ml-auto text-slate-400 hover:text-white lg:hidden" onClick={() => setMobileOpen(false)} aria-label="Close menu">
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="mt-4 flex-1 space-y-1 px-3">
          {nav.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              title={label}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                `group flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition ${
                  isActive ? 'bg-brand-500 text-white shadow-lg shadow-brand-500/25' : 'hover:bg-white/5 hover:text-white'
                }`
              }
            >
              <Icon className="h-5 w-5 shrink-0 transition group-hover:scale-110" />
              <span className={hideLabel}>{label}</span>
            </NavLink>
          ))}
        </nav>

        <button
          className="m-3 hidden items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white lg:flex"
          onClick={() => setCollapsed((c) => !c)}
        >
          {collapsed ? <PanelLeftOpen className="h-5 w-5" /> : <PanelLeftClose className="h-5 w-5" />}
          <span className={hideLabel}>Collapse</span>
        </button>
      </aside>

      <div className={`transition-all duration-300 ${collapsed ? 'lg:pl-[76px]' : 'lg:pl-64'}`}>
        <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-slate-200/70 bg-white/80 px-4 backdrop-blur sm:px-8">
          <button className="icon-btn lg:hidden" onClick={() => setMobileOpen(true)} aria-label="Open menu">
            <Menu className="h-5 w-5" />
          </button>
          <span className="text-sm font-semibold text-slate-700">{current?.label || 'EduCore'}</span>
          <span className="ml-auto hidden text-sm text-slate-500 sm:block">
            {new Date().toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
          </span>
        </header>
        <main className="mx-auto max-w-7xl p-4 sm:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
