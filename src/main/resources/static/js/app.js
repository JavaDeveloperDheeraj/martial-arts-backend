/**
 * 
 */
const API = {
  // Auth & OTP
  sendOtp: async (mobile, role) => {
    const url = role === 'STUDENT' ? `/api/otp/send?mobile=${mobile}&role=STUDENT` : `/api/auth/send-otp?mobile=${mobile}`;
    const res = await fetch(url);
    return { ok: res.ok, text: await res.text() };
  },

  verifyLogin: async (mobile, otp) => {
    const res = await fetch('/api/auth/verify-login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mobile, otp })
    });
    return { ok: res.ok, data: await res.json() };
  },

  studentLogin: async (mobile, otp) => {
    const res = await fetch(`/api/student/login?mobile=${mobile}&otp=${otp}`, { method: 'POST' });
    return { ok: res.ok, data: res.ok ? await res.json() : await res.text() };
  },

  // Student Registration
  registerStudent: async (formData) => {
    const res = await fetch('/api/student/register', { method: 'POST', body: formData });
    return { ok: res.ok, text: await res.text() };
  },

  // Admin Actions
  getPendingStudents: async () => (await fetch('/api/admin/students/pending')).json(),
  getApprovedStudents: async () => (await fetch('/api/admin/students/approved')).json(),
  approveStudent: async (id) => (await fetch(`/api/admin/students/approve/${id}`, { method: 'POST' })).text(),
  rejectStudent: async (id) => (await fetch(`/api/admin/students/reject/${id}`, { method: 'POST' })).text(),

  // Fee Management
  setupFee: async (payload) => {
    const res = await fetch('/api/fees/setup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return { ok: res.ok, text: await res.text() };
  },

  payFee: async (payload) => {
    const res = await fetch('/api/fees/pay', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return { ok: res.ok, text: await res.text() };
  },

  getFeeSummary: async (studentId) => (await fetch(`/api/fees/summary/${studentId}`)).json()
};