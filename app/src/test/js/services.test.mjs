import test from "node:test";
import assert from "node:assert/strict";

globalThis.window = { location: { origin: "https://clinic.example" } };

const doctor = await import("../../main/resources/static/js/services/doctorServices.js");
const patient = await import("../../main/resources/static/js/services/patientServices.js");
const appointment = await import("../../main/resources/static/js/services/appointmentRecordService.js");
const { readJson } = await import("../../main/resources/static/js/services/response.js");

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" }
  });
}

test("doctor listing and filters use API responses", async () => {
  const requests = [];
  globalThis.fetch = async url => {
    requests.push(url);
    return json({ doctors: [{ id: 3, name: "Dr. Lee" }] });
  };
  assert.equal((await doctor.getDoctors())[0].name, "Dr. Lee");
  assert.equal((await doctor.filterDoctors("Lee Smith", "AM", "Cardiology")).doctors[0].id, 3);
  assert.deepEqual(requests, [
    "https://clinic.example/doctor",
    "https://clinic.example/doctor/filter/Lee%20Smith/AM/Cardiology"
  ]);
});

test("doctor mutations report failures and never claim success", async () => {
  globalThis.fetch = async () => json({ message: "Not authorized" }, 403);
  await assert.rejects(doctor.deleteDoctor(3, "token"), /Not authorized/);
  await assert.rejects(doctor.saveDoctor({ name: "Lee" }, "token"), /Not authorized/);
  await assert.rejects(doctor.deleteDoctor(3, ""), /Please log in/);
});

test("patient requests encode path values and preserve failures", async () => {
  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    return json(url.endsWith("/login") ? { token: "t" }
      : url.endsWith("/patient") ? { message: "Registered" }
      : url.includes("/filter/") || url.includes("/doctor/") ? { appointments: [] }
      : { patient: { id: 2 } });
  };
  assert.deepEqual(await patient.patientSignup({ name: "Jane" }), { success: true, message: "Registered" });
  assert.equal((await patient.patientLogin({ email: "jane@example.com" })).ok, true);
  assert.equal((await patient.getPatientData("a/b")).id, 2);
  assert.deepEqual(await patient.getPatientAppointments(2, "a/b", "doctor"), []);
  assert.deepEqual((await patient.filterAppointments(null, "Jane Doe", "a/b")).appointments, []);
  assert.equal(requests[0].options.method, "POST");
  assert.equal(requests[1].options.method, "POST");
  assert.equal(requests[2].url, "https://clinic.example/patient/a%2Fb");
  assert.equal(requests[3].url, "https://clinic.example/patient/2/doctor/a%2Fb");
  assert.equal(requests[4].url, "https://clinic.example/patient/filter/null/Jane%20Doe/a%2Fb");

  globalThis.fetch = async () => json({ message: "Unavailable" }, 503);
  await assert.rejects(patient.getPatientData("token"), /Unavailable/);
  await assert.rejects(patient.patientSignup({}), /Unavailable/);
});

test("malformed success responses fail visibly", async () => {
  globalThis.fetch = async () => json({});
  await assert.rejects(doctor.getDoctors(), /missing a doctors list/);
  await assert.rejects(patient.getPatientAppointments(2, "token", "patient"), /missing appointments/);
  await assert.rejects(readJson(new Response("<html>error</html>", { status: 200 })), /Invalid response/);
});

test("doctor appointment search encodes patient names and reports errors", async () => {
  let url;
  globalThis.fetch = async requestedUrl => {
    url = requestedUrl;
    return json({ appointments: [] });
  };
  assert.deepEqual((await appointment.getAllAppointments("2026-09-25", "Jane/Smith", "a/b")).appointments, []);
  assert.equal(url, "https://clinic.example/appointments/2026-09-25/Jane%2FSmith/a%2Fb");
  globalThis.fetch = async () => json({ message: "Denied" }, 403);
  await assert.rejects(appointment.getAllAppointments("2026-09-25", "Jane", "token"), /403/);
});

test("role login uses the correct endpoints and stores tokens only on success", async () => {
  const values = { username: "staff", email: "doctor@example.com", password: "secret" };
  const stored = new Map();
  const requests = [];
  const alerts = [];
  const roles = [];
  globalThis.document = {
    getElementById(id) {
      return {
        value: values[id],
        addEventListener() {}
      };
    }
  };
  globalThis.localStorage = { setItem: (key, value) => stored.set(key, value) };
  globalThis.selectRole = role => roles.push(role);
  window.alert = message => alerts.push(message);
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    return json({ token: "issued-token" });
  };
  await import("../../main/resources/static/js/services/index.js");
  await window.adminLoginHandler();
  await window.doctorLoginHandler();
  assert.deepEqual(requests.map(request => request.url), [
    "https://clinic.example/admin",
    "https://clinic.example/doctor/login"
  ]);
  assert.deepEqual(roles, ["admin", "doctor"]);
  assert.equal(stored.get("token"), "issued-token");
  assert.deepEqual(JSON.parse(requests[0].options.body), { username: "staff", password: "secret" });

  stored.clear();
  globalThis.fetch = async () => json({ message: "Invalid credentials" }, 401);
  await window.adminLoginHandler();
  assert.equal(stored.has("token"), false);
  assert.match(alerts[0], /Invalid credentials/);
});
