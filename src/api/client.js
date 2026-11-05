import axios from 'axios';

export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const http = axios.create({ baseURL: API_BASE, timeout: 10000 });
const data = (r) => r.data;

/** Human readable message for any failed request, including "backend not running". */
export function errorMessage(err) {
  if (err?.response) {
    const body = err.response.data || {};
    if (body.fieldErrors && Object.keys(body.fieldErrors).length) return Object.values(body.fieldErrors).join('. ');
    return body.message || body.detail || `Request failed (HTTP ${err.response.status})`;
  }
  if (err?.request) return `Cannot reach the EduCore API at ${API_BASE}. Make sure the Spring Boot backend and MongoDB are running.`;
  return err?.message || 'Something went wrong';
}

/** Per-field validation errors from a 400 response: { fullName: 'Full name is required' } */
export function fieldErrors(err) {
  return err?.response?.data?.fieldErrors || {};
}

export const studentsApi = {
  list: (params) => http.get('/students', { params }).then(data),
  get: (id) => http.get(`/students/${id}`).then(data),
  create: (body) => http.post('/students', body).then(data),
  update: (id, body) => http.put(`/students/${id}`, body).then(data),
  remove: (id) => http.delete(`/students/${id}`).then(data),
};

export const classesApi = {
  list: () => http.get('/classes').then(data),
  create: (body) => http.post('/classes', body).then(data),
  update: (id, body) => http.put(`/classes/${id}`, body).then(data),
  remove: (id) => http.delete(`/classes/${id}`).then(data),
};

export const subjectsApi = {
  list: (params) => http.get('/subjects', { params }).then(data),
  create: (body) => http.post('/subjects', body).then(data),
  update: (id, body) => http.put(`/subjects/${id}`, body).then(data),
  remove: (id) => http.delete(`/subjects/${id}`).then(data),
};

export const attendanceApi = {
  list: (params) => http.get('/attendance', { params }).then(data),
  stats: (params) => http.get('/attendance/stats', { params }).then(data),
  saveBulk: (body) => http.post('/attendance/bulk', body).then(data),
};

export const dashboardApi = {
  summary: (params) => http.get('/dashboard/summary', { params }).then(data),
};
