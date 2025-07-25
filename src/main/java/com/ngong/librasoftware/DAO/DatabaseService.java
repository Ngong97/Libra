package com.ngong.librasoftware.DAO;


import com.ngong.librasoftware.model.*;
import com.ngong.librasoftware.view.CheckOutView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseService {

    private Connection connection;
    private String DB_URL;

    public DatabaseService() {
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + "/libraDB/currentDB/libra.db";

        this.DB_URL = "jdbc:sqlite:" + dbPath;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Connection initialized; you may add validation here if needed
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                showInfo("✅ Connected to database.");
            }
        } catch (SQLException e) {
            showError("❌ Failed to connect: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                showInfo("🔌 Database disconnected.");
            }
        } catch (SQLException e) {
            showError("❌ Error while disconnecting: " + e.getMessage());
        } finally {
            connection = null;
        }
    }

    public void setDatabasePath(String newPath) {
        disconnect(); // Clean shutdown
        this.DB_URL = "jdbc:sqlite:" + newPath;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connect();
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Database Status");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private String querySingle(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }


    //......this one


    public int countActiveStudents() {
        String sql = """
                    SELECT COUNT(DISTINCT s.student_id)
                    FROM students s
                    JOIN borrowings b ON b.student_id = s.student_id
                    WHERE b.returned = 0
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public int countActiveMales() {
        String sql = """
                    SELECT COUNT(DISTINCT s.student_id)
                    FROM students s
                    JOIN borrowings b ON b.student_id = s.student_id
                    WHERE b.returned = 0 AND s.gender="Male"
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countActiveFemales() {
        String sql = """
                    SELECT COUNT(DISTINCT s.student_id)
                    FROM students s
                    JOIN borrowings b ON b.student_id = s.student_id
                    WHERE b.returned = 0 AND s.gender="Female"
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


public int countOverdueBooks(String name, String grade, String gender, String term, LocalDate date, String title, String author) {
    int count = 0;
    StringBuilder query = new StringBuilder(
            "SELECT SUM(bd.quantity) FROM borrowings bd " +
                    "JOIN students s ON bd.student_id = s.student_id " +
                    "JOIN books b ON bd.book_id = b.book_id " +
                    "WHERE bd.returned = 0"
    );

    if (!name.isEmpty()) query.append(" AND s.name LIKE ?");
    if (!grade.isEmpty()) query.append(" AND s.class LIKE ?");
    if (!gender.isEmpty()) query.append(" AND s.gender LIKE ?");
    if (!term.isEmpty()) query.append(" AND bd.term LIKE ?");
    if (date != null) query.append(" AND bd.borrow_date >= ?");
    if (!title.isEmpty()) query.append(" AND b.title LIKE ?");
    if (!author.isEmpty()) query.append(" AND b.author LIKE ?");

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

        int index = 1;
        if (!name.isEmpty()) pstmt.setString(index++, "%" + name + "%");
        if (!grade.isEmpty()) pstmt.setString(index++, grade + "%");
        if (!gender.isEmpty()) pstmt.setString(index++, gender + "%");
        if (!term.isEmpty()) pstmt.setString(index++, term + "%");
        if (date != null) pstmt.setDate(index++, Date.valueOf(date));
        if (!title.isEmpty()) pstmt.setString(index++, "%" + title + "%");
        if (!author.isEmpty()) pstmt.setString(index++, "%" + author + "%");

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            count = rs.getInt(1); // Total quantity of overdue books
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return count;
}


    public String getMostReadBookTitle() {
        String sql = """
        SELECT LOWER(bk.title) AS normalized_title
        FROM borrowings b
        JOIN books bk ON bk.book_id = b.book_id
        WHERE b.returned = 0
        GROUP BY normalized_title
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return toTitleCase(rs.getString("normalized_title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }



    public String getMostActiveGender() {
        String sql = """
        SELECT s.gender, COUNT(*) AS count
        FROM borrowings b
        JOIN students s ON s.student_id = b.student_id
        WHERE b.returned = 0
        GROUP BY s.gender
        ORDER BY count DESC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Integer> genderCounts = new LinkedHashMap<>();

            while (rs.next()) {
                String gender = rs.getString("gender");
                int count = rs.getInt("count");
                genderCounts.put(gender, count);
            }

            if (genderCounts.isEmpty()) return "N/A";

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(genderCounts.entrySet());
            int topCount = entries.get(0).getValue();

            // Check for tie
            long tieCount = entries.stream()
                    .filter(e -> e.getValue() == topCount)
                    .count();

            if (tieCount > 1) {
                return "Neutral";
            } else {
                return entries.get(0).getKey() + " (Active)";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "N/A";
    }



    public String getTopReadingClass() {
        return querySingle("""
        SELECT s.student_class
        FROM students s
        WHERE s.student_id IN (
            SELECT b.student_id
            FROM borrowings b
            WHERE b.returned = 0
        )
        GROUP BY s.student_class
        ORDER BY COUNT(DISTINCT s.student_id) DESC
        LIMIT 1
    """);
    }





    public int countTotalInventory() {
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement("SELECT SUM(total_quantity) FROM library_books");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public List<String> getTopBorrowedBooks() {
        String sql = """
        SELECT LOWER(bk.title) AS normalized_title
        FROM borrowings b
        JOIN books bk ON bk.book_id = b.book_id
        WHERE b.returned = 0
        GROUP BY normalized_title
        ORDER BY COUNT(*) DESC
        LIMIT 5
    """;

        List<String> books = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String title = rs.getString("normalized_title");
                // Optionally title-case it again if you want nicer visuals:
                books.add(toTitleCase(title));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Optional beautifier
    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) return "";
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 1) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase());
            } else {
                sb.append(w.toUpperCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }




    public Map<String, List<String>> getRecentActivities() {
        String sql = """
        SELECT s.name, bk.title
        FROM borrowings b
        JOIN students s ON b.student_id = s.student_id
        JOIN books bk ON bk.book_id = b.book_id
        WHERE b.borrow_date IS NOT NULL
        ORDER BY b.borrow_date DESC
        LIMIT 5
    """;

        Map<String, Set<String>> map = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String book = rs.getString("title");

                map.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert Set to List
        Map<String, List<String>> result = new LinkedHashMap<>();
        map.forEach((name, books) -> result.put(name, new ArrayList<>(books)));
        return result;
    }


    public List<StudentRecord> getStudentBorrowingRecords() {
        List<StudentRecord> list = new ArrayList<>();
        String sql = """
SELECT
    s.student_id,
    s.identity,
    s.name,
    s.gender,
    s.student_class,
    COALESCE(MAX(br.term), '—') AS term,
    COALESCE(MIN(br.borrow_date), '—') AS borrow_date,
    COALESCE(MAX(br.return_date), '—') AS return_date,
    COALESCE(GROUP_CONCAT(
        CASE
            WHEN br.quantity > 1 THEN b.title || ' (' || br.quantity || ' copies)'
            ELSE b.title
        END,
        ', '
    ), '—') AS book_titles,
    COALESCE(GROUP_CONCAT(DISTINCT b.author), '—') AS authors,
    COALESCE(GROUP_CONCAT(DISTINCT b.isbn), '—') AS isbns,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM borrowings b2
            WHERE b2.student_id = s.student_id
              AND b2.returned = 0
              AND julianday('now') - julianday(b2.borrow_date) > b2.duration
        )
        THEN 'Delayed'
        ELSE 'On Time'
    END AS status
FROM students s
JOIN borrowings br ON br.student_id = s.student_id
JOIN books b ON br.book_id = b.book_id
WHERE br.returned = 0
GROUP BY s.student_id
ORDER BY s.name
""";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int sn = 1;
            while (rs.next()) {
                list.add(new StudentRecord(
                        sn++,
                        rs.getString("name"),
                        rs.getString("identity"),
                        rs.getInt("student_id"),
                        rs.getString("gender"),
                        rs.getString("student_class"),
                        rs.getString("term"),
                        rs.getString("book_titles"),
//                        rs.getString("book_titles")+" ("+book.quantity+") ",

                        rs.getString("authors"),
                        rs.getString("isbns"),
                        rs.getString("status"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public List<Book> getAllLibraryBooksWithAvailability() {

                String sql = """
            SELECT lb.book_id, lb.title, lb.author, lb.total_quantity,
                   COALESCE(SUM(CASE WHEN bd.returned = 0 THEN bd.quantity ELSE 0 END), 0) AS borrowed,
                   (lb.total_quantity - COALESCE(SUM(CASE WHEN bd.returned = 0 THEN bd.quantity ELSE 0 END), 0)) AS available
            FROM library_books lb
            LEFT JOIN books b ON LOWER(lb.title) = LOWER(b.title)
            LEFT JOIN borrowings bd ON b.book_id = bd.book_id
            GROUP BY lb.book_id, lb.title, lb.author, lb.total_quantity;
        """;

        List<Book> books = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int sn=1;
            while (rs.next()) {
                books.add(new Book(
                        sn++,
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_quantity"),
                        rs.getInt("borrowed"),
                        rs.getInt("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    private boolean titlesMatch(String t1, String t2) {
        return t1.trim().equalsIgnoreCase(t2.trim());
    }

    private boolean authorsSimilar(String a1, String a2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(normalize(a1).split(" ")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(normalize(a2).split(" ")));
        tokens1.retainAll(tokens2);
        return tokens1.size() >= 2;
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z ]", "").trim();
    }


    // Add new book or increment if already exists
    public void addOrIncrementLibraryBook(String title, String author, int quantity) {
        String sqlFind = "SELECT book_id, total_quantity FROM library_books WHERE LOWER(title) = LOWER(?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement find = conn.prepareStatement(sqlFind);
            find.setString(1, title);
            ResultSet rs = find.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                int total = rs.getInt("total_quantity");
                PreparedStatement update = conn.prepareStatement("UPDATE library_books SET total_quantity = ? WHERE book_id = ?");
                update.setInt(1, total + quantity);
                update.setInt(2, bookId);
                update.executeUpdate();
            } else {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO library_books (title, author, total_quantity) VALUES (?, ?, ?)");
                insert.setString(1, title);
                insert.setString(2, author);
                insert.setInt(3, quantity);
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrUpdateLibraryBook(String title, String author, int quantity) {
        String findSql = "SELECT book_id, title, author FROM library_books";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement find = conn.prepareStatement(findSql);
             ResultSet rs = find.executeQuery()) {

            boolean similarExists = false;

            while (rs.next()) {
                String existingTitle = rs.getString("title");
                String existingAuthor = rs.getString("author");

                if (titlesMatch(title, existingTitle) && authorsSimilar(author, existingAuthor)) {
                    similarExists = true;

                    int id = rs.getInt("book_id");

                    PreparedStatement update = conn.prepareStatement("""
                    UPDATE library_books
                    SET title = ?, author = ?, total_quantity = ?
                    WHERE book_id = ?
                """);
                    update.setString(1, title);
                    update.setString(2, author);
                    update.setInt(3, quantity); // REPLACE, don't add
                    update.setInt(4, id);
                    update.executeUpdate();
                    break;
                }
            }

            if (!similarExists) {
                PreparedStatement insert = conn.prepareStatement("""
                INSERT INTO library_books (title, author, total_quantity)
                VALUES (?, ?, ?)
            """);
                insert.setString(1, title);
                insert.setString(2, author);
                insert.setInt(3, quantity);
                insert.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void addLibraryBookIfMissing(String title, String author, int quantity) {
        String findSql = "SELECT title, author FROM library_books";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement find = conn.prepareStatement(findSql);
             ResultSet rs = find.executeQuery()) {

            boolean similarExists = false;

            while (rs.next()) {
                String existingTitle = rs.getString("title");
                String existingAuthor = rs.getString("author");

                if (titlesMatch(title, existingTitle) && authorsSimilar(author, existingAuthor)) {
                    similarExists = true;
                    break;
                }
            }

            if (!similarExists) {
                PreparedStatement insert = conn.prepareStatement("""
                INSERT INTO library_books (title, author, total_quantity)
                VALUES (?, ?, ?)
            """);
                insert.setString(1, title);
                insert.setString(2, author);
                insert.setInt(3, quantity);
                insert.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public String getLastUsedTerm() {
        String sql = "SELECT term FROM borrowings ORDER BY borrow_date DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getString("term") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    public List<CheckInEntry> getBorrowedBooksForCheckIn(int studentId) {
        List<CheckInEntry> books = new ArrayList<>();

            String sql = """
        SELECT br.borrowing_id, br.book_id, b.title, b.author, b.isbn, br.return_date
        FROM borrowings br
        JOIN books b ON br.book_id = b.book_id
        WHERE br.student_id = ? AND br.returned = 0
    """;


        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new CheckInEntry(
                        rs.getInt("borrowing_id"),
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("return_date")
                ));


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    public void updateStudentDetails(int studentId, String name, String identity,String gender, String studentClass /* no term */) {
        String sql = """
        UPDATE students
        SET name = ?,identity = ?, gender = ?, student_class = ?
        WHERE student_id = ?
    """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, identity);
            stmt.setString(3, gender);
            stmt.setString(4, studentClass);
            stmt.setInt(5, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBorrowedBookDetails(int bookId, String title, String author, String isbn) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ? WHERE book_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            stmt.setInt(4, bookId);
            int rows = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateBorrowingTerm(int studentId,String term) {
        String sql = """
        UPDATE borrowings
        SET term = ?
        WHERE student_id = ? AND returned = 0
    """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, term);
            stmt.setInt(2, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void clearReturnedBooks(List<Integer> borrowingIds) {
        String sql = "UPDATE borrowings SET returned = 1 WHERE borrowing_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int id : borrowingIds) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public void issueBookToStudent(String identity, String name, String gender, String studentClass, String term, CheckOutView.BookEntry2 book) {
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
        conn.setAutoCommit(false);

        int studentId = -1;

        // Try inserting student
        PreparedStatement insertStudent = conn.prepareStatement("""
            INSERT INTO students (identity, name, gender, student_class)
            VALUES (?, ?, ?, ?)
        """, Statement.RETURN_GENERATED_KEYS);
        insertStudent.setString(1, identity);
        insertStudent.setString(2, name);
        insertStudent.setString(3, gender);
        insertStudent.setString(4, studentClass);

        try {
            insertStudent.executeUpdate();
            ResultSet keys = insertStudent.getGeneratedKeys();
            if (keys.next()) {
                studentId = keys.getInt(1); // New student inserted
            }
        } catch (SQLException ex) {
            // Likely duplicate identity — fetch existing student_id
            PreparedStatement fetchStudentId = conn.prepareStatement("""
                SELECT student_id FROM students WHERE identity = ?
            """);
            fetchStudentId.setString(1, identity);
            ResultSet rs = fetchStudentId.executeQuery();
            if (rs.next()) {
                studentId = rs.getInt("student_id");
            }
        }


        // Try inserting book
        int bookId = -1;

// Insert new book
        PreparedStatement insertBook = conn.prepareStatement("""
    INSERT INTO books (title, author, isbn)
    VALUES (?, ?, ?)
""");
        insertBook.setString(1, book.title);
        insertBook.setString(2, book.author);
        insertBook.setString(3, book.isbn);
        insertBook.executeUpdate();

// Get the last inserted book_id
        Statement getId = conn.createStatement();
        ResultSet rs = getId.executeQuery("SELECT last_insert_rowid()");
        if (rs.next()) {
            bookId = rs.getInt(1);
        }



        if (bookId != -1) {
            PreparedStatement borrow = conn.prepareStatement("""
                INSERT INTO borrowings (student_id, book_id, borrow_date, term, quantity, duration, return_date, returned)
                VALUES (?, ?, date('now'), ?, ?, ?, date('now', '+' || ? || ' days'), 0)
            """);



            borrow.setInt(1, studentId);
            borrow.setInt(2, bookId);
            borrow.setString(3, term);
            borrow.setInt(4, book.quantity);
            borrow.setInt(5, Integer.parseInt(book.duration));
            borrow.setInt(6, Integer.parseInt(book.duration));
            borrow.executeUpdate();

            PreparedStatement log = conn.prepareStatement("""
                INSERT INTO activity_log (description) VALUES (?)
            """);
            log.setString(1, name + " borrowed — \"" + book.title + "\" for " + book.duration + " days");
            log.executeUpdate();
        }

        conn.commit();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public int saveBook(CheckOutView.BookEntry2 book) throws SQLException {
        String sql = "INSERT OR IGNORE INTO books (title, author, isbn) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.title);
            stmt.setString(2, book.author);
            stmt.setString(3, book.isbn);
            stmt.executeUpdate();
            return stmt.getGeneratedKeys().getInt(1);
        }
    }

    public int countReturnedBooks() {
        String sql = "SELECT COUNT(*) FROM borrowings WHERE returned = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }



    public Map<String, Integer> getActiveStudentsByClass() {
        String sql = """
        SELECT s.student_class AS class_name, COUNT(DISTINCT s.student_id) AS active_count
        FROM borrowings b
        JOIN students s ON s.student_id = b.student_id
        WHERE b.returned = 0
        GROUP BY s.student_class
        ORDER BY active_count DESC
    """;

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String className = rs.getString("class_name");
                int count = rs.getInt("active_count");
                result.put(className, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Integer> getActiveStudentsByClass(String term, String studentClass, Month month) {
        StringBuilder sql = new StringBuilder("""
        SELECT s.student_class AS class_name, COUNT(DISTINCT s.student_id) AS active_count
        FROM borrowings b
        JOIN students s ON s.student_id = b.student_id
        WHERE b.returned = 0
    """);

        List<Object> params = new ArrayList<>();

        if (!"All".equalsIgnoreCase(term)) {
            sql.append(" AND b.term = ?");
            params.add(term);
        }

        if (!"All".equalsIgnoreCase(studentClass)) {
            sql.append(" AND s.student_class = ?");
            params.add(studentClass);
        }

        if (month != null) {
            sql.append(" AND strftime('%m', b.borrow_date) = ?");
            params.add(String.format("%02d", month.getValue()));
        }

        sql.append(" GROUP BY s.student_class ORDER BY active_count DESC");

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("class_name"), rs.getInt("active_count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String> getAllTerms() {
        String sql = "SELECT DISTINCT term FROM borrowings WHERE term IS NOT NULL ORDER BY term";
        List<String> terms = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                terms.add(rs.getString("term"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return terms;
    }

    public List<String> getAllStudentClasses() {
        String sql = "SELECT DISTINCT student_class FROM students WHERE student_class IS NOT NULL ORDER BY student_class";
        List<String> classes = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                classes.add(rs.getString("student_class"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public List<String> getAllStudentGenders() {
        String sql = "SELECT DISTINCT gender FROM students WHERE gender IS NOT NULL ORDER BY gender";
        List<String> genders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                genders.add(rs.getString("gender"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genders;
    }

    public List<String> getAllStudentTerms() {
        String sql = "SELECT DISTINCT term FROM borrowings WHERE term IS NOT NULL ORDER BY term";
        List<String> genders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                genders.add(rs.getString("term"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genders;
    }



    public boolean hasNameWithUnreturnedBooks(String inputName) {
        String query = "SELECT student_id, name FROM students";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            String normalizedInput = inputName.trim().replaceAll("\\s+", "").toLowerCase();

            while (rs.next()) {
                String dbName = rs.getString("name");
                String studentId = rs.getString("student_id");

                String normalizedDbName = dbName.trim().replaceAll("\\s+", "").toLowerCase();

                if (normalizedDbName.equals(normalizedInput)) {
                    // Match found — check for unreturned books
                    String borrowQuery = "SELECT 1 FROM borrowings WHERE student_id = ? AND returned = 0";
                    try (PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery)) {
                        borrowStmt.setString(1, studentId);
                        ResultSet borrowRS = borrowStmt.executeQuery();

                        if (borrowRS.next()) {
                            return true; // Found a match with unreturned books
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }



    public void updateLibraryBook(int bookId, String title, String author, int total) {
        String sql = "UPDATE library_books SET title = ?, author = ?, total_quantity = ? WHERE book_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, total);
            stmt.setInt(4, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateLibraryBooks(String originalTitle, String originalAuthor,
                                  String newTitle, String newAuthor, int newQuantity) {
        String sql = """
        UPDATE library_books
        SET title = ?, author = ?, total_quantity = ?
        WHERE title = ? AND author = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setString(2, newAuthor);
            stmt.setInt(3, newQuantity);
            stmt.setString(4, originalTitle);
            stmt.setString(5, originalAuthor);

            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                // Optional: if no matching row found, insert new
                addOrUpdateLibraryBook(newTitle, newAuthor, newQuantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


public void clearReturnStatusForBooks(int borrowerId, Map<Integer, Integer> quantityMap) {
    String selectQuery = """
        SELECT borrowing_id, quantity, returned
        FROM borrowings
        WHERE student_id = ? AND book_id = ? AND returned = 0
        ORDER BY quantity DESC
    """;

    String updateQuery = """
        UPDATE borrowings
        SET quantity = ?, returned = ?
        WHERE borrowing_id = ?
    """;

    try (Connection connection = DriverManager.getConnection(DB_URL);
         PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
         PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

        connection.setAutoCommit(false);

        for (Map.Entry<Integer, Integer> entry : quantityMap.entrySet()) {
            int bookId = entry.getKey();
            int quantityToClear = entry.getValue();

            selectStmt.setInt(1, borrowerId);
            selectStmt.setInt(2, bookId);

            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int recordId = rs.getInt("borrowing_id");
                int currentQty = rs.getInt("quantity");

                if (quantityToClear >= currentQty) {
                    updateStmt.setInt(1, 0);      // quantity becomes 0
                    updateStmt.setInt(2, 1);      // mark returned
                    updateStmt.setInt(3, recordId);
                    updateStmt.addBatch();
                    quantityToClear -= currentQty;
                } else if (quantityToClear > 0) {
                    int newQty = currentQty - quantityToClear;
                    updateStmt.setInt(1, newQty);
                    updateStmt.setInt(2, 0);      // still active
                    updateStmt.setInt(3, recordId);
                    updateStmt.addBatch();
                    quantityToClear = 0;
                }

                if (quantityToClear <= 0) break;
            }
        }

        updateStmt.executeBatch();
        connection.commit();

    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Error", "Failed to clear return status.");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            rollbackEx.getSuppressed();
        }
    }
}

    public Map<Integer, Integer> getBorrowedQuantities(int studentId, List<Integer> bookIds) {
        Map<Integer, Integer> quantities = new HashMap<>();
        String sql = """
        SELECT book_id, SUM(quantity) AS total_borrowed
        FROM borrowings
        WHERE student_id = ? AND returned = 0 AND book_id IN (%s)
        GROUP BY book_id
    """.formatted(bookIds.stream().map(id -> "?").collect(Collectors.joining(",")));

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            for (int i = 0; i < bookIds.size(); i++) {
                stmt.setInt(i + 2, bookIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                int quantity = rs.getInt("total_borrowed");
                quantities.put(bookId, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantities;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public int getStudentIdWithUnreturnedBooks(String studentName) {
        int studentId = -1; // Will store the student_id if found
        String query = "SELECT s.student_id " + "FROM students s " + "JOIN borrowings b ON s.student_id = b.student_id " + "WHERE s.name = ? AND b.returned = 0 " + "LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set the student name parameter
            preparedStatement.setString(1, studentName);
            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If a result is found, get the student_id
                if (resultSet.next()) { studentId = resultSet.getInt("student_id");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return studentId;
    }

    public List<Integer> getBookIdsForStudent(int studentId) {
        List<Integer> bookIds = new ArrayList<>();
        String query = "SELECT book_id FROM borrowings WHERE student_id = ? AND returned = 0";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set the student_id parameter
            preparedStatement.setInt(1, studentId);
            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Loop through the result set and collect book_ids
                while (resultSet.next()) {
                    bookIds.add(resultSet.getInt("book_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookIds;
    }
    public Map<Integer, String> getBookTitlesForIds(List<Integer> bookIds) {
        Map<Integer, String> bookIdTitleMap = new HashMap<>();

        // SQL query to retrieve book titles for the given list of book IDs
        String query = "SELECT book_id, title FROM books WHERE book_id IN (" + String.join(",", Collections.nCopies(bookIds.size(), "?")) + ")";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set book IDs as parameters for the SQL query
            for (int i = 0; i < bookIds.size(); i++) {
                preparedStatement.setInt(i + 1, bookIds.get(i));
            }
            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Populate the bookIdTitleMap with retrieved book titles
                while (resultSet.next()) {
                    int bookId = resultSet.getInt("book_id");
                    String title = resultSet.getString("title");
                    bookIdTitleMap.put(bookId, title);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookIdTitleMap;
    }
    //................................



    public String getAuthorByTitleFromDictionary(String title) {
        String query = "SELECT author FROM bookDictionary WHERE title LIKE ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("author");
            }
        } catch (SQLException e) {
            e.printStackTrace(); }
        return "";
    }

    public void loadTitles(ComboBox<String> titleComboBox) {
        titleComboBox.getItems().clear();
        String query = "SELECT DISTINCT title FROM books";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String title = rs.getString("title"); // Add the title to the ComboBox only if it doesn't already exist (case-insensitive)
                if (!titleComboBox.getItems().stream().anyMatch(t -> t.equalsIgnoreCase(title))) {
                    titleComboBox.getItems().add(title);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<BookInfo> getRecentlyBorrowedBooksWithAuthors() {
        List<BookInfo> list = new ArrayList<>();

        String query = """
        SELECT DISTINCT title, author
        FROM books
        WHERE title IS NOT NULL AND title != ''
        ORDER BY book_id DESC
        LIMIT 10
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author") != null ? rs.getString("author") : "Unknown";
                list.add(new BookInfo(title, author));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public void addToBookDictionary(String title, String author, String description) throws SQLException {
        String query = "INSERT INTO bookDictionary (title, author, description) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title.trim());
            stmt.setString(2, author.trim());
            stmt.setString(3, description == null ? null : description.trim()); // ✅ safe
            stmt.executeUpdate();
        }
    }

    public boolean recordExists(String title, String author) {
        boolean exists = false;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bookDictionary WHERE LOWER(title) = LOWER(?) AND LOWER(author) = LOWER(?)")) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next(); // This checks if there's at least one record
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public boolean nameGenderExists(String name, String gender) {
        boolean exists = false;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM gender_memory WHERE LOWER(name) = LOWER(?) AND LOWER(gender) = LOWER(?)")) {
            stmt.setString(1, name);
            stmt.setString(2, gender);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next(); // This checks if there's at least one record
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }


    public int countUnclearedStudentsByGender(String gender, String term, String studentClass, Month month) {
        StringBuilder query = new StringBuilder("""
        SELECT COUNT(DISTINCT s.student_id)
        FROM students s
        JOIN borrowings b ON s.student_id = b.student_id
        WHERE b.returned = 0 AND s.gender = ?
    """);

        if (term != null) query.append(" AND s.term = ?");
        if (studentClass != null) query.append(" AND s.class = ?");
        if (month != null) query.append(" AND strftime('%m', b.borrow_date) = ?");

        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(query.toString())) {
            int index = 1;
            stmt.setString(index++, gender);
            if (term != null) stmt.setString(index++, term);
            if (studentClass != null) stmt.setString(index++, studentClass);
            if (month != null) stmt.setString(index++, String.format("%02d", month.getValue()));

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }





    public void exportUniqueBookTitlesToFile(File file) {
        String query = """
        SELECT title, author, COUNT(*) AS quantity
        FROM books
        GROUP BY title, author
        ORDER BY title COLLATE NOCASE
    """;

        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(query);
             ResultSet rs = stmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int quantity = rs.getInt("quantity");

                String line = String.format("%s - %s = %d", title, author, quantity);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteLibraryBookById(int bookId) {
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement("DELETE FROM library_books WHERE book_id = ?")) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void storeUsedNameParts(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isBlank()) {
                insertUsedNameIfNotExists(part);
            }
        }
    }

    private void insertUsedNameIfNotExists(String namePart) {
        String sql = "INSERT OR IGNORE INTO used_names(name) VALUES(?)";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, namePart);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> getStudentNamesStartingWith(String prefix) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT name FROM used_names WHERE name LIKE ? ORDER BY name LIMIT 10";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }


    public String getAuthorForBook(String title) {
        String sql = "SELECT author FROM books WHERE title = ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("author");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public String getAuthorForBookFromBookDictionary(String title) {
        String sql = "SELECT author FROM bookDictionary WHERE title LIKE ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("author");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }


    public String getShortDescription(String title) {
        String sql = "SELECT description FROM bookDictionary WHERE title LIKE ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCoverImagePath(String title) {
        String sql = "SELECT cover_image FROM bookDictionary WHERE title = ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("cover_image");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "/images/covers/default_cover.jpg"; // fallback
    }

    public String getCoverImagePathForDeveloper(String title) {
        String sql = "SELECT image FROM developer WHERE name = ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("image");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "/images/user.png"; // fallback
    }


    public List<String> getRecentBorrowersForBook(String title) {
        List<String> borrowers = new ArrayList<>();
        String sql = """
        SELECT DISTINCT s.name,bk.title
        FROM borrowings b
        JOIN students s ON b.student_id = s.student_id
        JOIN books bk ON bk.book_id = b.book_id
        WHERE bk.title = ? AND b.returned = 0
        ORDER BY b.borrow_date DESC
        LIMIT 5
    """;
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                borrowers.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowers;
    }

    public List<String> getRecentlyClearedStudents(int limit) {
        List<String> cleared = new ArrayList<>();
        String sql = """
        SELECT DISTINCT s.name
        FROM students s
        JOIN borrowings b ON s.student_id = b.student_id
        WHERE s.student_id NOT IN (
            SELECT student_id FROM borrowings WHERE returned = 0
        )
        AND b.returned = 1
        ORDER BY b.return_date DESC
        LIMIT ?
    """;
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cleared.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cleared;
    }

    public  ReportData fetchReportData() {
        ReportData data = new ReportData();

        String totalBooksQuery = "SELECT SUM(total_quantity) FROM library_books";
        String borrowedBooksQuery = "SELECT COUNT(*) FROM borrowings WHERE returned = 0";

        int total = 0;
        int borrowed = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Total registered books
            try (PreparedStatement stmt = conn.prepareStatement(totalBooksQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }

            // Total borrowed books (not yet returned)
            try (PreparedStatement stmt = conn.prepareStatement(borrowedBooksQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    borrowed = rs.getInt(1);
                }
            }

            int available = total - borrowed;

            data.setRegistered(total);
            data.setBorrowed(borrowed);
            data.setRemaining(Math.max(available, 0)); // Avoid negative values

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }




    public Map<String, String> getDefaultLibrarianInfo() {
        Map<String, String> info = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT librarian, school FROM users LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                info.put("librarian", rs.getString("librarian"));
                info.put("school", rs.getString("school"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }
    public String getCurrentUserSchool() {
        String sql = "SELECT school FROM users LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("school");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }



    public String getCurrentUserPassword() {
        String sql = "SELECT password FROM users LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public String getCurrentUserFullName() {
        String sql = "SELECT librarian FROM users LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("librarian");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }


    public boolean updateUserInfo(String fullName, String password) {
        String sql = "UPDATE users SET librarian = ?, password = ? WHERE id = (SELECT id FROM users LIMIT 1)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDescriptionForDeveloper(String category) {
        String sql = "SELECT description FROM developer WHERE name LIKE ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public String getDescriptionForCategory(String category) {
        String sql = "SELECT description FROM categories WHERE cat_name LIKE ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("description");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    public void addToPendingDescriptions(String title, String author) {
        String query = "INSERT OR IGNORE INTO pendingDescriptions (title, author) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title.trim());
            stmt.setString(2, author.trim());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PendingBook> getPendingBooks() {
        List<PendingBook> pending = new ArrayList<>();
        String query = "SELECT title, author, retryCount FROM pendingDescriptions";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int retryCount = rs.getInt("retryCount");

                pending.add(new PendingBook(title, author, retryCount));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pending;
    }


    public void updateBookDescription(String title, String author, String description) {
        String query = "UPDATE bookDictionary SET description = ? WHERE title = ? AND author = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, description.trim());
            stmt.setString(2, title.trim());
            stmt.setString(3, author.trim());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removeFromPending(String title, String author) {
        String query = "DELETE FROM pendingDescriptions WHERE title = ? AND author = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title.trim());
            stmt.setString(2, author.trim());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String fetchBookDescription(String title, String author) {
        String query = URLEncoder.encode("intitle:" + title + " inauthor:" + author, StandardCharsets.UTF_8);
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        try (InputStream is = new URL(apiUrl).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);

            JSONObject root = new JSONObject(json.toString());
            JSONArray items = root.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                    String desc = volumeInfo.optString("description", "");
                    if (!desc.isBlank()) {
                        return desc;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return generateOfflineCategoryDescription(title);
    }

    public void incrementRetryCount(String title, String author) {
        String query = "UPDATE pendingDescriptions SET retryCount = retryCount + 1 WHERE title = ? AND author = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectedToInternet() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (Exception e) {
            return false;
        }
    }


    public String fetchSchoolName() {
        String sql = "SELECT school FROM users LIMIT 1"; // Assumes single-school setup
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("school");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown School";
    }

    public boolean resetPassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetUsername(String email, String oldUsername, String newUsername) {
        String sql = "UPDATE users SET librarian = ? WHERE email = ? AND librarian = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, email);
            pstmt.setString(3, oldUsername);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetUsernameAndPassword(String email, String oldUsername, String newUsername, String newPassword) {
        String sql = "UPDATE user SET librarian = ?, password = ? WHERE email = ? AND librarian = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newPassword);
            pstmt.setString(3, email);
            pstmt.setString(4, oldUsername);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkCredentials(String librarian, String password) {
        boolean credentialsMatch = false;
        try (Connection conn = DriverManager.getConnection(DB_URL);) {
            String query = "SELECT * FROM users WHERE librarian = ? AND password = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, librarian);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    credentialsMatch = resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return credentialsMatch;
    }


    // Method to check if the user table in the database has data
    public boolean checkIfUserTableHasData() {
        String query = "SELECT COUNT(*) FROM users";

        try (
                // Establishing a connection to your database
                Connection connection = DriverManager.getConnection(DB_URL);

                // Creating a statement object
                Statement statement = connection.createStatement();

                // Executing the query and getting the result set
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            // If there is at least one row in the result set, return true (table has data)
            if (resultSet.next()) {
                int rowCount = resultSet.getInt(1);
                return rowCount > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If there was an error or no rows in the result set, return false (table is empty or error occurred)
        return false;
    }

    public boolean userExists(String librarianName) {
        String query = "SELECT COUNT(*) FROM users WHERE librarian = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, librarianName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserPassword(String librarianName, String newPassword) {
        String updateQuery = "UPDATE users SET password = ? WHERE librarian = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, librarianName);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Method to save user data to the database
    public void saveUserToDatabase(String username, String password, String school) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO users (librarian, password,school) VALUES (?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, school);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            e.getSuppressed();
        }
    }

    public List<BookInfo> getBookDetailsForTitleStartingWith(String prefix) {
        List<BookInfo> books = new ArrayList<>();

        String query = "SELECT title, author FROM bookDictionary WHERE title LIKE ? ORDER BY title LIMIT 10";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, prefix + "%"); // prefix match
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                books.add(new BookInfo(title, author));
            }

        } catch (SQLException e) {
            e.printStackTrace(); // or log the error
        }

        return books;
    }
    public List<String> getOfflineSampleBooks() {
        List<String> samples = new ArrayList<>();
        String sql = "SELECT title, author FROM offline_samples ORDER BY RANDOM() LIMIT 15";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Random rand = new Random();

            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int quantity = rand.nextInt(10) + 1; // Random quantity between 1 and 10
                samples.add(title + " - " + author + " = " + quantity);
            }

        } catch (SQLException e) {
            samples.add("⚠️ Error loading from offline_samples: " + e.getMessage());
        }

        return samples;
    }


    public void restoreReturnStatusForBooks(int studentId, Map<Integer, Integer> quantityMap) {
        String selectQuery = """
        SELECT borrowing_id, quantity, returned
        FROM borrowings
        WHERE student_id = ? AND book_id = ? 
        ORDER BY quantity ASC
    """;

        String updateQuery = """
        UPDATE borrowings
        SET quantity = ?, returned = ?
        WHERE borrowing_id = ?
    """;

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            for (Map.Entry<Integer, Integer> entry : quantityMap.entrySet()) {
                int bookId = entry.getKey();
                int quantityToRestore = entry.getValue();

                selectStmt.setInt(1, studentId);
                selectStmt.setInt(2, bookId);

                ResultSet rs = selectStmt.executeQuery();

                while (rs.next()) {
                    int borrowId = rs.getInt("borrowing_id");
                    int currentQty = rs.getInt("quantity");
                    int returnedFlag = rs.getInt("returned");

                    if (returnedFlag == 1) {
                        updateStmt.setInt(1, quantityToRestore); // resurrect full quantity
                        updateStmt.setInt(2, 0); // mark as active
                        updateStmt.setInt(3, borrowId);
                        updateStmt.addBatch();
                        break;
                    } else {
                        updateStmt.setInt(1, currentQty + quantityToRestore);
                        updateStmt.setInt(2, 0);
                        updateStmt.setInt(3, borrowId);
                        updateStmt.addBatch();
                        break;
                    }
                }
            }

            updateStmt.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to undo clearance.");
            try (Connection connection = DriverManager.getConnection(DB_URL)) {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }



public List<BookEntry> getUniqueBorrowedBooks() {
    List<BookEntry> entries = new ArrayList<>();
    String query = "SELECT title, author FROM books";

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String title = rs.getString("title");
            String author = rs.getString("author");

            boolean alreadyCaptured = entries.stream().anyMatch(existing ->
                    titlesMatch(existing.title(), title) &&
                            authorsSimilar(existing.author(), author)
            );

            if (!alreadyCaptured) {
                entries.add(new BookEntry(title, author, 100));
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return entries;
}






    public boolean hasUnreturnedBooks(String studentName) {
        String sql = """
        SELECT 1
        FROM students s
        JOIN borrowings b ON s.student_id = b.student_id
        WHERE s.name = ? AND b.returned = 0
        LIMIT 1
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentName);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if any unreturned books
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public String generateFallbackIsbn(String title, String author) {
    String base = title.trim().toLowerCase() + "|" + author.trim().toLowerCase();
    int salt = 0;
    String isbn;

    try (Connection conn = DriverManager.getConnection(DB_URL)) {
        while (true) {
            String candidate = base + "|" + salt;
            int hash = Math.abs(candidate.hashCode());
            isbn = "LIBRA-" + hash;

            PreparedStatement check = conn.prepareStatement("""
                SELECT 1 FROM books WHERE isbn = ?
            """);
            check.setString(1, isbn);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) break; // ISBN is unique — we're good
            salt++; // Try another variation
        }
    } catch (SQLException e) {
        e.printStackTrace();
        isbn = "LIBRA-FALLBACK";
    }

    return isbn;
}

    public boolean doesIsbnExist(String isbn) {
        String sql = "SELECT 1 FROM books WHERE isbn = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if a row was found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public boolean doesStudentIdExistAndHasUncleared(String studentId) {
        String sql = """
        SELECT s.student_id
        FROM students s
        JOIN borrowings b ON s.student_id = b.student_id
        WHERE s.identity = ? AND b.returned = 0
        LIMIT 1
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if the student exists and has uncleared borrowings

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doesStudentNameExistAndHasUncleared(String name) {
        String sql = """
        SELECT s.student_id
        FROM students s
        JOIN borrowings b ON s.student_id = b.student_id
        WHERE s.name = ? AND b.returned = 0
        LIMIT 1
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if the student exists and has uncleared borrowings

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public String getGenderFromMemory(String name) {
        String queryExact = "SELECT gender FROM gender_memory WHERE name = ?";
        String queryFirstName = "SELECT gender FROM gender_memory WHERE name LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 1. Exact match
            try (PreparedStatement exactStmt = conn.prepareStatement(queryExact)) {
                exactStmt.setString(1, name);
                ResultSet exactRs = exactStmt.executeQuery();
                if (exactRs.next()) {
                    return exactRs.getString("gender");
                }
            }

            // 2. Fallback: Match by first name
            String[] parts = name.split("\\s+"); // split by whitespace
            if (parts.length > 0) {
                String firstName = parts[0];

                try (PreparedStatement fuzzyStmt = conn.prepareStatement(queryFirstName)) {
                    fuzzyStmt.setString(1, firstName + "%"); // names starting with firstName
                    ResultSet fuzzyRs = fuzzyStmt.executeQuery();

                    if (fuzzyRs.next()) {
                        return fuzzyRs.getString("gender");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



    public void saveGenderToMemory(String name, String gender) {
        String query = "INSERT INTO gender_memory (name, gender) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, gender);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String generateOnlineCategoryDescription(String title) {
        try {
            String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = "https://openlibrary.org/search.json?q=" + query;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String response = reader.lines().collect(Collectors.joining());

                JSONObject json = new JSONObject(response);
                JSONArray docs = json.getJSONArray("docs");
                if (!docs.isEmpty()) {
                    JSONObject book = docs.getJSONObject(0);
                    if (book.has("subject")) {
                        JSONArray subjects = book.getJSONArray("subject");
                        return "A book focused on: " + subjects.join(", ").replaceAll("\"", "") + ".";
                    }
                }
            }
        } catch (Exception e) {
            // fallback if the API fails
        }
        return generateOfflineCategoryDescription(title);
    }


    public int addNewBook(String title, String author, String isbn) throws SQLException {
        String insertSql = "INSERT INTO books (title, author, isbn) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title.trim());
            stmt.setString(2, author.trim());

            // Handle optional ISBN input
            String cleanedIsbn = (isbn == null || isbn.trim().isEmpty()) ? null : isbn.trim();
            stmt.setString(3, cleanedIsbn);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1); // ✅ Return generated book_id
                } else {
                    throw new SQLException("❌ Book inserted but ID not returned.");
                }
            }

        } catch (SQLException ex) {
            // Optional: Add refined error reporting or fallback logic here
            throw new SQLException("❌ Failed to insert new book: " + ex.getMessage(), ex);
        }
    }


    private String generateOfflineCategoryDescription(String title) {
        title = title.toLowerCase();

        if (title.contains("physics") || title.contains("energy") || title.contains("mechanics") || title.contains("optics") || title.contains("thermodynamics") || title.contains("quantum"))
            return "An educational resource in the physical sciences.";

        if (title.contains("poetry") || title.contains("verse") || title.contains("sonnet") || title.contains("ballad") || title.contains("rhyme"))
            return "A lyrical collection of expressive writing.";

        if (title.contains("history") || title.contains("civilization") || title.contains("ancient") || title.contains("warfare") || title.contains("medieval") || title.contains("empire") || title.contains("revolution"))
            return "A historical narrative reflecting past events and figures.";

        if (title.contains("math") || title.contains("algebra") || title.contains("geometry") || title.contains("calculus") || title.contains("trigonometry") || title.contains("statistics") || title.contains("probability") || title.contains("arithmetic"))
            return "A foundational guide to mathematical reasoning.";

        if (title.contains("biology") || title.contains("life") || title.contains("cell") || title.contains("botany") || title.contains("zoology") || title.contains("genetics") || title.contains("ecology"))
            return "A scientific exploration of living systems.";

        if (title.contains("computing") || title.contains("technology") || title.contains("programming") || title.contains("network") || title.contains("algorithm") || title.contains("software") || title.contains("hardware") || title.contains("cybersecurity"))
            return "A technical resource on modern digital tools.";

        if (title.contains("chemistry") || title.contains("chemical") || title.contains("organic") || title.contains("inorganic") || title.contains("reaction") || title.contains("molecule") || title.contains("element"))
            return "A scientific resource on chemical sciences.";

        if (title.contains("english") || title.contains("grammar") || title.contains("vocabulary") || title.contains("literature") || title.contains("writing") || title.contains("comprehension"))
            return "A linguistic resource in the English language.";

        if (title.contains("jungle") || title.contains("wilderness") || title.contains("forest") || title.contains("expedition") || title.contains("safari") || title.contains("adventure"))
            return "A narrative or resource showcasing the explorations of jungles.";

        if (title.contains("novel") || title.contains("fiction") || title.contains("mystery") || title.contains("thriller") || title.contains("romance") || title.contains("drama") || title.contains("horror") || title.contains("fantasy"))
            return "A fictional narrative intended for entertainment or reflection.";

        if (title.contains("psychology") || title.contains("mind") || title.contains("behavior") || title.contains("mental") || title.contains("cognition") || title.contains("therapy"))
            return "A resource on human behavior and mental processes.";

        if (title.contains("philosophy") || title.contains("ethics") || title.contains("logic") || title.contains("metaphysics") || title.contains("epistemology"))
            return "A contemplative resource exploring thought, knowledge, and existence.";

        if (title.contains("sociology") || title.contains("society") || title.contains("culture") || title.contains("social") || title.contains("community"))
            return "A resource on human societies and their structures.";

        if (title.contains("religion") || title.contains("bible") || title.contains("quran") || title.contains("faith") || title.contains("god") || title.contains("spiritual"))
            return "A religious or spiritual resource addressing faith and belief systems.";

        if (title.contains("economics") || title.contains("finance") || title.contains("trade") || title.contains("business") || title.contains("money") || title.contains("market"))
            return "A financial or economic analysis resource.";

        if (title.contains("politics") || title.contains("government") || title.contains("democracy") || title.contains("constitution") || title.contains("law") || title.contains("policy"))
            return "A resource on political systems, governance, or legal matters.";

        if (title.contains("engineering") || title.contains("mechanical") || title.contains("electrical") || title.contains("civil") || title.contains("software engineering") || title.contains("robotics") || title.contains("circuit"))
            return "A technical manual or textbook in the field of engineering.";

        if (title.contains("medicine") || title.contains("nursing") || title.contains("anatomy") || title.contains("physiology") || title.contains("diagnosis") || title.contains("treatment") || title.contains("surgery"))
            return "A medical resource for healthcare studies and practices.";

        if (title.contains("health") || title.contains("fitness") || title.contains("nutrition") || title.contains("wellness") || title.contains("exercise"))
            return "A resource promoting personal health and well-being.";

        if (title.contains("agriculture") || title.contains("farming") || title.contains("horticulture") || title.contains("soil") || title.contains("crop"))
            return "A resource on agricultural science and practices.";

        if (title.contains("geography") || title.contains("climate") || title.contains("earth") || title.contains("continent") || title.contains("landform") || title.contains("environment"))
            return "An educational guide to the Earth and its environments.";

        if (title.contains("astronomy") || title.contains("galaxy") || title.contains("vacuum") || title.contains("outer space") || title.contains("orbit") || title.contains("universe"))
            return "A scientific resource exploring the universe and space phenomena.";

        if (title.contains("art") || title.contains("painting") || title.contains("drawing") || title.contains("sculpture") || title.contains("design"))
            return "A creative resource in visual arts and aesthetics.";

        if (title.contains("music") || title.contains("instrument") || title.contains("composition") || title.contains("melody") || title.contains("rhythm"))
            return "A resource on musical expression and theory.";

        if (title.contains("drama") || title.contains("theatre") || title.contains("acting") || title.contains("performance"))
            return "A theatrical resource related to performing arts.";

        if (title.contains("education") || title.contains("pedagogy") || title.contains("teaching") || title.contains("learning") || title.contains("curriculum"))
            return "A scholarly resource on the science of education.";

        if (title.contains("programming") || title.contains("java")|| title.contains("html")  || title.contains("python") || title.contains("c++") || title.contains("web development") || title.contains("coding"))
            return "A resource for learning programming languages and development skills.";

        if (title.contains("data") || title.contains("machine learning") || title.contains("artificial intelligence") || title.contains("ai") || title.contains("analytics") || title.contains("big data"))
            return "An advanced resource on data science and AI technologies.";

        if (title.contains("entrepreneurship") || title.contains("startup") || title.contains("innovation") || title.contains("leadership") || title.contains("management"))
            return "A guide to business innovation, leadership, and entrepreneurial ventures.";

        if (title.contains("law") || title.contains("legal") || title.contains("rule of law") || title.contains("court") || title.contains("criminal"))
            return "A reference in legal studies and judicial systems.";

        if (title.contains("engineering drawing") || title.contains("technical drawing") || title.contains("blueprint"))
            return "A technical resource on engineering drawing and design representation.";

        if (title.contains("travel") || title.contains("guide") || title.contains("tourism") || title.contains("destination"))
            return "A travel guide or tourism-focused reference.";

        if (title.contains("dictionary") || title.contains("lexicon") || title.contains("thesaurus") || title.contains("encyclopedia"))
            return "A reference resource for words and general knowledge.";

        // ... add more if you want more than 600
// 1. Mythology & Folklore
        if (title.contains("myth") || title.contains("folklore") || title.contains("legend") || title.contains("tale") || title.contains("epic"))
            return "A collection of traditional myths, legends, and epic tales.";

// 2. Climate & Environment
        if (title.contains("climate") || title.contains("ecology") || title.contains("sustainability") || title.contains("greenhouse") || title.contains("conservation"))
            return "A guide on environmental science and climate awareness.";

// 3. Gender & Identity Studies
        if (title.contains("gender") || title.contains("feminism") || title.contains("masculinity") || title.contains("identity") || title.contains("queer"))
            return "A sociological exploration of gender and identity issues.";

// 4. Artificial Intelligence & Robotics
        if (title.contains("robotics") || title.contains("ai") || title.contains("deep learning") || title.contains("neural network") || title.contains("deep learning"))
            return "An insightful resource on intelligent machines and algorithms.";

// 5. Film & Cinema
        if (title.contains("cinema") || title.contains("film") || title.contains("screenplay") || title.contains("movie") || title.contains("director"))
            return "A detailed dive into the art and craft of filmmaking.";

// 6. Cryptography & Cybersecurity
        if (title.contains("cybersecurity") || title.contains("encryption") || title.contains("cryptography") || title.contains("hacking") || title.contains("network security"))
            return "A technical guide to digital security and cryptography.";

// 7. Cultural Anthropology
        if (title.contains("anthropology") || title.contains("tribe") || title.contains("culture") || title.contains("ethnography") || title.contains("ritual"))
            return "An anthropological study of cultures and human societies.";

// 8. Transportation & Vehicles
        if (title.contains("automobile") || title.contains("car") || title.contains("transportation") || title.contains("vehicle") || title.contains("logistics"))
            return "An overview of transportation systems and vehicle technologies.";

// 9. Astronomy & Space Science
        if (title.contains(" cosmic science") || title.contains("space") || title.contains("planet") || title.contains("cosmos") || title.contains("astrophysics"))
            return "An astronomical guide to the cosmos and celestial bodies.";

// 10. Ethics & Moral Philosophy
        if (title.contains("ethics") || title.contains("morality") || title.contains("virtue") || title.contains("justice") || title.contains("conscience"))
            return "A philosophical exploration of ethics, justice, and morality.";

// 11. Social Media & Internet Culture
        if (title.contains("social media") || title.contains("influencer") || title.contains("instagram") || title.contains("tiktok") || title.contains("viral"))
            return "A commentary on digital culture and the social media era.";

// 12. Photography & Visual Arts
        if (title.contains("photography") || title.contains("lens") || title.contains("exposure") || title.contains("portrait") || title.contains("dslr"))
            return "A visual guide to the techniques and beauty of photography.";

// 13. Fashion & Style
        if (title.contains("fashion") || title.contains("runway") || title.contains("style") || title.contains("couture") || title.contains("trend"))
            return "A stylish look into the world of fashion and trends.";

// 14. Agriculture & Farming
        if (title.contains("cultivation") || title.contains(" agricultural science") || title.contains("gathering of crops")|| title.contains("harvesting period") || title.contains("reaping") || title.contains("livestock"))
            return "A practical guide to agriculture, farming, and food production.";

// 15. Journalism & Media Studies
        if (title.contains("journalism") || title.contains("press") || title.contains("newsroom") || title.contains("reporting") || title.contains("media"))
            return "An exploration of journalism and the power of the press.";

// 16. Architecture & Urban Planning
        if (title.contains("designing and building") || title.contains("urban") || title.contains("design"))
            return "A look into modern architecture and city design.";

// 17. Psychology & Human Behavior
        if (title.contains("behavioral science") || title.contains("behavior") || title.contains("mindset") || title.contains("therapy") || title.contains("cognition"))
            return "A guide to understanding human behavior and mental processes.";

// 18. Languages & Linguistics
        if (title.contains("linguistics") || title.contains("language") || title.contains("grammar") || title.contains("syntax") || title.contains("phonetics"))
            return "An introduction to language structure and linguistic theory.";

// 19. Interior Design & Home Living
        if (title.contains("interior") || title.contains("furniture") || title.contains("decor") || title.contains("home") || title.contains("aesthetic"))
            return "Tips and ideas for creating beautiful interior spaces.";

// 20. Music Theory & Production
        if (title.contains("music") || title.contains("melody") || title.contains("rhythm") || title.contains("composition") || title.contains("audio production"))
            return "A creative look at musical theory and sound production.";
        if (title.contains("neuroscience") || title.contains("brain") || title.contains("cognitive") || title.contains("neuron") || title.contains("synapse"))
            return "An advanced exploration into cognitive science and brain function.";

        if (title.contains("astronomy") || title.contains("universe") || title.contains("stars") || title.contains("galaxies") || title.contains("cosmos"))
            return "A cosmic journey through space, stars, and the mysteries of the universe.";

        if (title.contains("architecture") || title.contains("blueprint") || title.contains("designing") || title.contains("urbanism") || title.contains("infrastructure"))
            return "A comprehensive study of structural design and architectural innovation.";

        if (title.contains("psychology") || title.contains("conduct") || title.contains("mental") || title.contains("therapy") || title.contains("emotions"))
            return "A psychological perspective into human behavior and mental health.";

        if (title.contains("entrepreneurship") || title.contains("startup") || title.contains("founder") || title.contains("venture") || title.contains("business model"))
            return "An inspirational guide to founding and managing innovative ventures.";

        if (title.contains("marine") || title.contains("ocean") || title.contains("sea life") || title.contains("aquatic") || title.contains("coral"))
            return "A scientific dive into marine biology and underwater ecosystems.";

        if (title.contains("aviation") || title.contains("flight") || title.contains("pilot") || title.contains("aerospace") || title.contains("aircraft"))
            return "A technical resource exploring the science and art of flight.";

        if (title.contains("farming") || title.contains("agriculture") || title.contains("harvest") || title.contains("crop") || title.contains("soil"))
            return "A practical manual on modern and traditional agricultural methods.";

        if (title.contains("finance") || title.contains("investment") || title.contains("banking") || title.contains("stock") || title.contains("capital"))
            return "An informative guide to financial systems, markets, and investments.";

        if (title.contains("sociology") || title.contains("society") || title.contains("culture") || title.contains("social") || title.contains("class"))
            return "A sociological examination of human societies and cultural dynamics.";

        if (title.contains("mythology") || title.contains("legend") || title.contains("folklore") || title.contains("pantheon") || title.contains("deities"))
            return "A mythical exploration into ancient stories and legendary beings.";

        if (title.contains("robotics") || title.contains("automation") || title.contains("mechatronics") || title.contains("servo") || title.contains("sensor"))
            return "A technical manual on robotic systems and intelligent automation.";

        if (title.contains("gender") || title.contains("feminism") || title.contains("masculinity") || title.contains("equality") || title.contains("identity"))
            return "A scholarly discussion of gender identity, roles, and social constructs.";

        if (title.contains("photography") || title.contains("camera") || title.contains("exposure") || title.contains("lens") || title.contains("shutter"))
            return "A visual guide to photography techniques and creative composition.";

        if (title.contains("astrology") || title.contains("zodiac") || title.contains("horoscope") || title.contains("starsign") || title.contains("planets"))
            return "An astrological interpretation of cosmic influence on human lives.";

        if (title.contains("philosophy") || title.contains("ethics") || title.contains("metaphysics") || title.contains("logic") || title.contains("existential"))
            return "A philosophical inquiry into existence, morality, and thought.";

        if (title.contains("criminology") || title.contains("crime") || title.contains("law enforcement") || title.contains("forensic") || title.contains("justice"))
            return "An analytical resource on criminal behavior and justice systems.";

        if (title.contains("nutrition") || title.contains("diet") || title.contains("food science") || title.contains("vitamins") || title.contains("calories"))
            return "A health-oriented guide to diet, nutrition, and bodily wellness.";

        if (title.contains("linguistics") || title.contains("syntax") || title.contains("phonetics") || title.contains("morphology") || title.contains("semantics"))
            return "A theoretical and practical resource on language structure and analysis.";

        if (title.contains("ethnography") || title.contains("tribe") || title.contains("indigenous") || title.contains("native") || title.contains("custom"))
            return "An ethnographic study of cultural groups and traditional practices.";

        if (title.contains("engineering") || title.contains("mechanics") || title.contains("civil") || title.contains("structural") || title.contains("thermodynamics"))
            return "An engineering manual covering the principles of design and mechanics.";

        if (title.contains("meteorology") || title.contains("weather") || title.contains("climate") || title.contains("storm") || title.contains("atmosphere"))
            return "A meteorological guide to weather patterns and atmospheric science.";

        if (title.contains("zoology") || title.contains("animals") || title.contains("wildlife") || title.contains("mammals") || title.contains("species"))
            return "A zoological study of the animal kingdom and natural habitats.";

        if (title.contains("anatomy") || title.contains("human body") || title.contains("organs") || title.contains("muscles") || title.contains("skeletal"))
            return "An anatomical guide to the structure of the human body.";

        if (title.contains("optics") || title.contains("light") || title.contains("magnifier") || title.contains("refraction") || title.contains("mirror"))
            return "A focused exploration of the physics of light and optical devices.";

        return "A general educational book useful for academic reference.";
    }

    public ResultSet getClearedStudentRecords() throws SQLException {
        String query = """
        SELECT
            s.name AS studentName,
            s.identity,
            s.gender,
            s.student_class AS class,
            br.term,
            GROUP_CONCAT(bk.title, ', ') AS booktitles,
            GROUP_CONCAT(bk.author, ', ') AS authors
        FROM students s
        JOIN borrowings br ON br.student_id = s.student_id
        JOIN books bk ON br.book_id = bk.book_id
        WHERE br.returned = 1
        GROUP BY s.student_id, br.term
        """;
        Connection conn=DriverManager.getConnection(DB_URL);
        PreparedStatement stmt = conn.prepareStatement(query); // connection is your DB handle
        return stmt.executeQuery();
    }


    public boolean removeClearedRecords() {
        String deleteBorrowings = "DELETE FROM borrowings WHERE returned = 1";

        String deleteStudents = """
        DELETE FROM students
        WHERE student_id NOT IN (
            SELECT student_id FROM borrowings
        )
        """;

        String deleteBooks = """
        DELETE FROM books
        WHERE book_id NOT IN (
            SELECT book_id FROM borrowings
        )
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Step 1: Remove cleared borrowings
            stmt.executeUpdate(deleteBorrowings);

            // Step 2: Remove students with no remaining borrowings
            stmt.executeUpdate(deleteStudents);

            // Step 3: Remove books no longer associated with any borrowing
            stmt.executeUpdate(deleteBooks);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean isValidPassword(String input) {
        String query = "SELECT 1 FROM users WHERE password = ?";
        try (PreparedStatement stmt = DriverManager.getConnection(DB_URL).prepareStatement(query)) {
            stmt.setString(1, input); // You can hash this if passwords are encrypted
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // True if there's a match
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getRegisteredBooksNo() {
        String sql = "SELECT SUM(total_quantity) FROM library_books";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int getRemainingBooksNo() {
        return getRegisteredBooksNo() - countOverdueBooks("","","","",null,"","");
    }


    public int countOverdueBooks() {
        int count = 0;
        String query = "SELECT SUM(bd.quantity) FROM borrowings bd " +
                "JOIN students s ON bd.student_id = s.student_id " +
                "JOIN books b ON bd.book_id = b.book_id " +
                "WHERE bd.returned = 0 AND bd.return_date < CURRENT_DATE";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1); // Total quantity of overdue books
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }


}
