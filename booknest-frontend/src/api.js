const API = "http://localhost:8080/api";

export function getToken() {
  return sessionStorage.getItem("booknestToken");
}

export async function request(endpoint, options = {}) {
  const token = getToken();

  const response = await fetch(`${API}${endpoint}`, {
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  const text = await response.text();
  let data = {};

  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { message: text };
  }

  if (!response.ok) {
    throw new Error(
      data.message || data.error || `Request failed (${response.status})`
    );
  }

  return data;
}

export const authAPI = {
  register: (details) =>
    request("/auth/register", {
      method: "POST",
      body: JSON.stringify(details),
    }),

  verifyOtp: (details) =>
    request("/auth/verify-otp", {
      method: "POST",
      body: JSON.stringify(details),
    }),

  login: (details) =>
    request("/auth/login", {
      method: "POST",
      body: JSON.stringify(details),
    }),
};

export default API;