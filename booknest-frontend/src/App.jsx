import { useEffect, useMemo, useState } from "react";
import { request } from "./api";
import Auth from "./Auth";
import "./App.css";

function Logo() {
  return (
    <div className="brand">
      <span className="brand-icon">
        <svg viewBox="0 0 64 64" aria-hidden="true">
          <path d="M6 15 Q20 9 31 19 V53 Q18 43 6 48Z" fill="#d8ad50" />
          <path d="M58 15 Q44 9 33 19 V53 Q46 43 58 48Z" fill="#f2d27d" />
          <path d="M32 19 V54" stroke="#fff7e5" strokeWidth="2" />
          <path d="M20 10 L25 14 L32 6 L39 14 L44 10" fill="none"
            stroke="#d8ad50" strokeWidth="3" strokeLinecap="round" />
        </svg>
      </span>
      <span>BookNest<span className="brand-dot">.</span></span>
    </div>
  );
}

function BookCover({ book, index = 0 }) {
  const [failed, setFailed] = useState(false);

  useEffect(() => setFailed(false), [book.coverUrl]);

  if (book.coverUrl && !failed) {
    return (
      <div className={`book-cover cover-${index % 5}`}>
        <img
          src={book.coverUrl}
          alt={`Cover of ${book.title}`}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      </div>
    );
  }

  return (
    <div className={`book-cover cover-${index % 5}`}>
      <span className="cover-monogram">B.</span>
      <div className="cover-center">
        <span className="cover-small">THE BOOKNEST COLLECTION</span>
        <h3>{book.title}</h3>
        <span className="cover-divider" />
        <p>{book.author || "Unknown author"}</p>
      </div>
    </div>
  );
}

export default function App() {
  const [token, setToken] = useState(
    () => sessionStorage.getItem("booknestToken")
  );
  const [email, setEmail] = useState(
    () => sessionStorage.getItem("booknestEmail") || ""
  );
  const [page, setPage] = useState("Dashboard");
  const [books, setBooks] = useState([]);
  const [borrowings, setBorrowings] = useState([]);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [selected, setSelected] = useState(null);
  const [busy, setBusy] = useState(false);
  const [showAdd, setShowAdd] = useState(false);
  const [newBook, setNewBook] = useState({
    title: "",
    author: "",
    category: "General",
  });

  function handleLogin(newToken, newEmail) {
    sessionStorage.setItem("booknestToken", newToken);
    sessionStorage.setItem("booknestEmail", newEmail);
    setToken(newToken);
    setEmail(newEmail);
  }

  function logout() {
    sessionStorage.removeItem("booknestToken");
    sessionStorage.removeItem("booknestEmail");
    setToken(null);
    setEmail("");
    setBooks([]);
    setBorrowings([]);
    setSelected(null);
  }

  async function loadBooks() {
    setLoading(true);
    setError("");

    try {
      const data = await request("/books");
      setBooks(Array.isArray(data) ? data : data.content || []);
    } catch (err) {
      setError(err.message || "Could not load books.");
    } finally {
      setLoading(false);
    }
  }

  async function loadBorrowings() {
    try {
      const data = await request("/borrowings/my");
      setBorrowings(Array.isArray(data) ? data : data.content || []);
    } catch (err) {
      setNotice("Borrowing history: " + err.message);
    }
  }

  useEffect(() => {
    if (token) {
      loadBooks();
      loadBorrowings();
    }
  }, [token]);

  const categories = useMemo(
    () => ["All", ...new Set(books.map((b) => b.category).filter(Boolean))],
    [books]
  );

  const filteredBooks = books.filter((book) => {
    const q = search.toLowerCase().trim();
    return (
      (book.title || "").toLowerCase().includes(q) ||
      (book.author || "").toLowerCase().includes(q) ||
      (category === "All" || book.category === category)
    );
  });

  const activeBorrowings = borrowings.filter((item) => {
    const status = String(item.status || "").toLowerCase();
    return !item.returned &&
      !item.returnDate &&
      !item.returnedAt &&
      !status.includes("return") &&
      !status.includes("complete") &&
      !status.includes("closed");
  });

  async function borrowBook(book) {
    setBusy(true);
    setNotice("");

    try {
      await request(`/borrowings/${book.id}`, { method: "POST" });
      setNotice(`"${book.title}" borrowed successfully.`);
      setSelected(null);
      await loadBooks();
      await loadBorrowings();
      setPage("My Borrowings");
    } catch (err) {
      setNotice("Could not borrow book: " + err.message);
    } finally {
      setBusy(false);
    }
  }

  async function returnBook(item) {
    const id = item.borrowingId ?? item.id;

    if (id == null) {
      setNotice("Could not identify the borrowing record.");
      return;
    }

    setBusy(true);
    setNotice("");

    try {
      await request(`/borrowings/${id}/return`, { method: "PUT" });
      setNotice("Book returned successfully.");
      await loadBooks();
      await loadBorrowings();
    } catch (err) {
      setNotice("Could not return book: " + err.message);
    } finally {
      setBusy(false);
    }
  }

  async function addBook(event) {
    event.preventDefault();
    setBusy(true);
    setNotice("");

    try {
      await request("/books", {
        method: "POST",
        body: JSON.stringify({
          ...newBook,
          available: true,
        }),
      });

      setNotice("Book added successfully.");
      setShowAdd(false);
      setNewBook({ title: "", author: "", category: "General" });
      await loadBooks();
    } catch (err) {
      setNotice("Could not add book: " + err.message);
    } finally {
      setBusy(false);
    }
  }

  async function deleteBook(book) {
    if (!window.confirm(`Delete "${book.title}" from the library?`)) return;

    setBusy(true);
    setNotice("");

    try {
      await request(`/books/${book.id}`, { method: "DELETE" });
      setNotice("Book deleted successfully.");
      setSelected(null);
      await loadBooks();
    } catch (err) {
      setNotice("Could not delete book: " + err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!token) return <Auth onLogin={handleLogin} />;

  const navigation = ["Dashboard", "Explore Books", "Available Books", "My Borrowings"];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Logo />
        <p className="side-heading">YOUR LIBRARY</p>

        <nav className="side-nav">
          {navigation.map((item, i) => (
            <button
              key={item}
              className={page === item ? "nav-link active" : "nav-link"}
              onClick={() => {
                setPage(item);
                setSelected(null);
                setNotice("");
              }}
            >
              <span>{["⌂", "▤", "✓", "♡"][i]}</span>
              {item}
            </button>
          ))}
        </nav>

        <div className="sidebar-note">
          <span className="eyebrow">A WORLD OF STORIES</span>
          <div className="note-art">✳</div>
          <h3>One more chapter?</h3>
          <p>Your next favourite book is waiting to be discovered.</p>
          <button onClick={() => setPage("Explore Books")}>
            Explore collection ↗
          </button>
        </div>

        <div className="user-profile">
          <span className="avatar">{email.charAt(0).toUpperCase() || "B"}</span>
          <div className="profile-email">{email}</div>
        </div>
        <button className="logout-button" onClick={logout}>↗ Sign out</button>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <span className="breadcrumb">BOOKNEST / {page.toUpperCase()}</span>
          <div className="top-actions">
            <span className="connection"><i /> Library connected</span>
            <button className="icon-button" onClick={() => {
              loadBooks();
              loadBorrowings();
            }} title="Refresh">↻</button>
          </div>
        </header>

        {notice && (
          <div className="notice" role="status">
            {notice}
            <button onClick={() => setNotice("")}>×</button>
          </div>
        )}

        {page === "Dashboard" && (
          <>
            <section className="hero">
              <div className="hero-copy">
                <span className="eyebrow">YOUR LITTLE CORNER OF THE WORLD</span>
                <h1>Stories look good<br />on <em>you.</em></h1>
                <p>Find a quiet moment, discover a new perspective, and let your next favourite book find you.</p>
                <button className="primary-button" onClick={() => setPage("Explore Books")}>
                  Explore the collection ↗
                </button>
              </div>
              <div className="hero-art" aria-label="Decorative books">
                <div className="hero-sun" />
                <div className="hero-circle" />
                <div className="hero-book hero-book-one">THE ART<br />OF SLOWING<br />DOWN</div>
                <div className="hero-book hero-book-two">A LITTLE<br />MORE MAGIC</div>
                <div className="hero-book hero-book-three">WILD<br />IDEAS</div>
                <span className="hero-star">✳</span>
              </div>
              <div className="hero-footer">READ MORE · WORRY LESS · REPEAT</div>
            </section>

            <section className="stats-grid">
              <button className="stat-card" onClick={() => setPage("Explore Books")}>
                <span>▤ In the collection</span>
                <strong>{loading ? "—" : books.length}</strong>
                <small>Books to discover ↗</small>
              </button>
              <button className="stat-card" onClick={() => setPage("Available Books")}>
                <span>✓ Ready for you</span>
                <strong>{books.filter((b) => b.available).length}</strong>
                <small>Available to borrow ↗</small>
              </button>
              <button className="stat-card" onClick={() => setPage("My Borrowings")}>
                <span>♡ On loan</span>
                <strong>{activeBorrowings.length}</strong>
                <small>Your current borrowings ↗</small>
              </button>
            </section>
          </>
        )}

        {(page === "Explore Books" || page === "Available Books") && (
          <section className="catalogue">
            <div className="page-intro">
              <span className="eyebrow">YOUR NEXT GREAT READ</span>
              <h1>{page === "Available Books" ? <>Books waiting<br /><em>for you.</em></> : <>Find your next<br /><em>favourite.</em></>}</h1>
              <p>Browse the shelves, follow your curiosity, and discover something unexpected.</p>
            </div>

            <div className="section-heading">
              <div>
                <span className="eyebrow">THE BOOKSHELF</span>
                <h2>{page === "Available Books" ? "Available now" : "Explore all books"}</h2>
                <p>{filteredBooks.length} books in this view</p>
              </div>
              <button className="primary-button" onClick={() => setShowAdd(!showAdd)}>
                {showAdd ? "Cancel" : "+ Add a book"}
              </button>
            </div>

            {showAdd && (
              <form className="add-book-form" onSubmit={addBook}>
                <input placeholder="Book title" value={newBook.title} required
                  onChange={(e) => setNewBook({ ...newBook, title: e.target.value })} />
                <input placeholder="Author" value={newBook.author} required
                  onChange={(e) => setNewBook({ ...newBook, author: e.target.value })} />
                <input placeholder="Category" value={newBook.category} required
                  onChange={(e) => setNewBook({ ...newBook, category: e.target.value })} />
                <button className="primary-button" disabled={busy}>Save book</button>
              </form>
            )}

            <div className="filters">
              <input value={search} onChange={(e) => setSearch(e.target.value)}
                placeholder="⌕  Search title or author..." />
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                {categories.map((c) => <option key={c}>{c}</option>)}
              </select>
            </div>

            {loading && <p className="empty-state">Loading your library...</p>}
            {error && <div className="form-alert error">{error} <button onClick={loadBooks}>Retry</button></div>}

            {!loading && !error && (
              <div className="book-grid">
                {filteredBooks
                  .filter((b) => page !== "Available Books" || b.available)
                  .map((book, index) => (
                    <article className="book-card" key={book.id}>
                      <button className="cover-button" onClick={() => setSelected(book)}>
                        <BookCover book={book} index={index} />
                      </button>
                      <div className="book-meta">
                        <span>{book.category || "General"}</span>
                        <span className={book.available ? "status available" : "status unavailable"}>
                          {book.available ? "Available" : "Borrowed"}
                        </span>
                      </div>
                      <h3>{book.title}</h3>
                      <p>by {book.author || "Unknown author"}</p>
                      <button className="text-button" onClick={() => setSelected(book)}>
                        Discover book ↗
                      </button>
                    </article>
                  ))}
              </div>
            )}
          </section>
        )}

        {page === "My Borrowings" && (
          <section className="catalogue">
            <div className="page-intro">
              <span className="eyebrow">A GOOD BOOK IS GOOD COMPANY</span>
              <h1>Your reading<br /><em>journey.</em></h1>
              <p>Keep track of the books you've borrowed from BookNest.</p>
            </div>
            <div className="section-heading">
              <div>
                <span className="eyebrow">YOUR READING LIST</span>
                <h2>My borrowings</h2>
                <p>{activeBorrowings.length} active borrowing records</p>
              </div>
              <button className="primary-button" onClick={() => setPage("Explore Books")}>
                Find another book →
              </button>
            </div>

            {activeBorrowings.length === 0 ? (
              <div className="empty-state">
                <span>♡</span>
                <h3>Your next chapter awaits</h3>
                <p>Books you borrow will appear here.</p>
              </div>
            ) : (
              <div className="borrowing-list">
                {activeBorrowings.map((item, index) => {
                  const bookId = item?.book?.id ?? item?.bookId ?? item?.id;
                  const book = item?.book || books.find((b) => String(b.id) === String(bookId));
                  const borrowingId = item.borrowingId ?? item.id;

                  return (
                    <article className="borrowing-row" key={borrowingId ?? index}>
                      <div className="borrowing-cover">
                        {book && <BookCover book={book} index={index} />}
                      </div>
                      <div className="borrowing-info">
                        <span className="eyebrow">CURRENTLY BORROWED</span>
                        <h3>{book?.title || item.bookTitle || "Borrowed book"}</h3>
                        <p>by {book?.author || item.author || "Unknown author"}</p>
                        <small>{String(item.borrowDate || item.borrowedAt || "").slice(0, 10)}</small>
                      </div>
                      <button className="secondary-button" disabled={busy}
                        onClick={() => returnBook(item)}>Return book ↗</button>
                    </article>
                  );
                })}
              </div>
            )}
          </section>
        )}

        <footer className="footer">
          <span>BOOKNEST © 2026</span>
          <span>A little more reading. A little more wonder. ✳</span>
        </footer>
      </main>

      {selected && (
        <div className="modal-backdrop" onClick={() => setSelected(null)}>
          <section className="book-modal" role="dialog" aria-modal="true"
            onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelected(null)}>×</button>
            <BookCover book={selected} index={Number(selected.id) || 0} />
            <div className="modal-details">
              <span className="eyebrow">{selected.category || "BOOKNEST COLLECTION"}</span>
              <h2>{selected.title}</h2>
              <p>by {selected.author || "Unknown author"}</p>
              <span className={selected.available ? "status available" : "status unavailable"}>
                {selected.available ? "Available to borrow" : "Currently borrowed"}
              </span>
              {selected.available && (
                <button className="primary-button" disabled={busy}
                  onClick={() => borrowBook(selected)}>
                  {busy ? "Please wait..." : "Borrow this book →"}
                </button>
              )}
              <button className="delete-button" disabled={busy}
                onClick={() => deleteBook(selected)}>
                Delete book
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  );
}