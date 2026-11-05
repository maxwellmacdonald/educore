import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Students from './pages/Students.jsx';
import StudentForm from './pages/StudentForm.jsx';
import StudentDetails from './pages/StudentDetails.jsx';
import Classes from './pages/Classes.jsx';
import Subjects from './pages/Subjects.jsx';
import Attendance from './pages/Attendance.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="students" element={<Students />} />
        <Route path="students/new" element={<StudentForm />} />
        <Route path="students/:id" element={<StudentDetails />} />
        <Route path="students/:id/edit" element={<StudentForm />} />
        <Route path="classes" element={<Classes />} />
        <Route path="subjects" element={<Subjects />} />
        <Route path="attendance" element={<Attendance />} />
        <Route path="*" element={<div className="card p-10 text-center text-slate-500">Page not found.</div>} />
      </Route>
    </Routes>
  );
}
