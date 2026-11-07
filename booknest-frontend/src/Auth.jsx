import { useState } from "react";
import { authAPI } from "./api";
import "./App.css";

export default function Auth({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    setBusy(true);

    try {
      if (mode === "register") {
        await authAPI.register({ name, email, password });
        setMode("verify");
        setMessage("Registration submitted. Check your email for the OTP.");
      } else if (mode === "verify") {
        const result = await authAPI.verifyOtp({ email, otp });
        setMessage(result?.message || "Email verified. Please sign in.");
        setMode("login");
      } else {
        const result = await authAPI.login({ email, password });

        if (!result?.token) {
          throw new Error("The login response did not contain a token.");
        }

        onLogin(result.token, email);
      }
    } catch (err) {
      setError(err.message || "Unable to complete the request.");
    } finally {
      setBusy(false);
    }
  }

  function changeMode(nextMode) {
    setMode(nextMode);
    setError("");
    setMessage("");
  }

  return (
    <main className="auth-page">
      <section className="auth-story">
        <a className="brand" href="#home" aria-label="BookNest home">
          <span className="brand-icon">
            <svg viewBox="0 0 64 64" role="img" aria-label="Open book logo">
              <path d="M6 15 Q20 9 31 19 V53 Q18 43 6 48Z" fill="#d8ad50" />
              <path d="M58 15 Q44 9 33 19 V53 Q46 43 58 48Z" fill="#f2d27d" />
              <path d="M32 19 V54" stroke="#fff7e5" strokeWidth="2" />
              <path d="M20 10 L25 14 L32 6 L39 14 L44 10" fill="none"
                stroke="#d8ad50" strokeWidth="3" strokeLinecap="round" />
            </svg>
          </span>
          <span>BookNest<span className="brand-dot">.</span></span>
        </a>

        <div className="story-copy">
          <span className="eyebrow">A LITTLE ESCAPE, EVERY DAY</span>
          <h1>Somewhere between the pages, you find yourself.</h1>
          <p>
            Discover books, collect ideas, and make time for stories.
          </p>
        </div>

        <div className="story-bottom">
          <span>READ</span>
          <span>WONDER</span>
          <span>GROW</span>
        </div>
        <div className="story-watermark">B</div>
      </section>

      <section className="auth-panel">
        <form className="auth-form" onSubmit={submit}>
          <span className="eyebrow">WELCOME TO BOOKNEST</span>

          <h2>
            {mode === "login"
              ? "Welcome back"
              : mode === "register"
                ? "Create your account"
                : "Verify your email"}
          </h2>

          <p className="auth-description">
            {mode === "login"
              ? "Sign in to continue your reading journey."
              : mode === "register"
                ? "Create your account and discover your next favourite book."
                : "Enter the OTP sent to your email address."}
          </p>

          {mode === "register" && (
            <label className="field">
              Full name
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your full name"
                autoComplete="name"
                required
              />
            </label>
          )}

          <label className="field">
            Email address
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              readOnly={mode === "verify"}
              required
            />
          </label>

          {mode === "verify" ? (
            <label className="field">
              Verification OTP
              <input
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                placeholder="Enter your OTP"
                inputMode="numeric"
                required
              />
            </label>
          ) : (
            <label className="field">
              Password
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                autoComplete={
                  mode === "login" ? "current-password" : "new-password"
                }
                minLength={mode === "register" ? 6 : undefined}
                required
              />
            </label>
          )}

          {error && <div className="form-alert error">{error}</div>}
          {message && <div className="form-alert success">{message}</div>}

          <button className="primary-button auth-submit" disabled={busy}>
            {busy
              ? "Please wait..."
              : mode === "login"
                ? "Sign in →"
                : mode === "register"
                  ? "Create account →"
                  : "Verify OTP →"}
          </button>

          <div className="auth-switch">
            {mode === "login" ? (
              <>
                New to BookNest?{" "}
                <button type="button" onClick={() => changeMode("register")}>
                  Create an account
                </button>
              </>
            ) : mode === "register" ? (
              <>
                Already registered?{" "}
                <button type="button" onClick={() => changeMode("login")}>
                  Sign in
                </button>
              </>
            ) : (
              <>
                Wrong email?{" "}
                <button type="button" onClick={() => changeMode("register")}>
                  Register again
                </button>
              </>
            )}
          </div>
        </form>
      </section>
    </main>
  );
}