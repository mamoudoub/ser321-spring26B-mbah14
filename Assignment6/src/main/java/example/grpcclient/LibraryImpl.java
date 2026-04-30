package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;
import com.google.protobuf.Empty;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;

/**
 * LibraryImpl
 *
 * Stateful gRPC Library service implementation.
 * Handles book listing, searching, borrowing, and returning.
 *
 * Persistence:
 * - Loads from books.txt on first run
 * - Loads from library_data.json on restart
 * - Saves after every borrow/return operation
 *
 * Bonus:
 * - Implements 7-day due date system (return_by field)
 *
 * Author: Mamoudou
 * Version: 1.1 (04/30/2026)
 */
class LibraryImpl extends LibraryGrpc.LibraryImplBase {

    private final Map<String, Book> library = new HashMap<>();
    private static final String DATA_FILE = "library_data.json";
    private static final String INIT_FILE = "books.txt";

    public LibraryImpl() {
        loadData();
    }

    // ===========================
    // HELPERS
    // ===========================
    private boolean isInvalid(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String calculateDueDate() {
        return LocalDate.now().plusDays(7).toString();
    }

    // ===========================
    // LIST BOOKS
    // ===========================
    @Override
    public void listBooks(Empty request, StreamObserver<BookListResponse> responseObserver) {

        BookListResponse.Builder response = BookListResponse.newBuilder();

        if (library.isEmpty()) {
            response.setIsSuccess(false)
                    .setError("no books in library yet");
        } else {
            response.setIsSuccess(true)
                    .addAllBooks(library.values());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // ===========================
    // SEARCH BOOKS
    // ===========================
    @Override
    public void searchBooks(BookSearchRequest request, StreamObserver<BookListResponse> responseObserver) {

        BookListResponse.Builder response = BookListResponse.newBuilder();

        String query = request.getQuery();

        if (isInvalid(query)) {
            response.setIsSuccess(false)
                    .setError("missing field");

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        query = query.trim().toLowerCase();

        List<Book> results = new ArrayList<>();

        for (Book book : library.values()) {
            if (book.getTitle().toLowerCase().contains(query) ||
                    book.getAuthor().toLowerCase().contains(query)) {
                results.add(book);
            }
        }

        if (results.isEmpty()) {
            response.setIsSuccess(false)
                    .setError("no books found matching query");
        } else {
            response.setIsSuccess(true)
                    .addAllBooks(results);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // ===========================
    // BORROW BOOK
    // ===========================
    @Override
    public void borrowBook(BorrowRequest request, StreamObserver<BorrowResponse> responseObserver) {

        BorrowResponse.Builder response = BorrowResponse.newBuilder();

        String isbn = request.getIsbn();
        String borrower = request.getBorrowerName();

        if (isInvalid(isbn) || isInvalid(borrower)) {
            response.setIsSuccess(false)
                    .setError("missing field");

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        isbn = isbn.trim();
        borrower = borrower.trim();

        Book book = library.get(isbn);

        if (book == null) {
            response.setIsSuccess(false)
                    .setError("book not found");

        } else if (book.getIsBorrowed()) {
            response.setIsSuccess(false)
                    .setError("book is already borrowed");

        } else {

            String dueDate = calculateDueDate();

            System.out.println("Book borrowed, due date: " + dueDate);

            Book updated = book.toBuilder()
                    .setIsBorrowed(true)
                    .setBorrowedBy(borrower)
                    .setReturnBy(dueDate)
                    .build();

            library.put(isbn, updated);
            saveData();

            response.setIsSuccess(true)
                    .setMessage("book borrowed successfully (due: " + dueDate + ")");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // ===========================
    // RETURN BOOK
    // ===========================
    @Override
    public void returnBook(ReturnRequest request, StreamObserver<ReturnResponse> responseObserver) {

        ReturnResponse.Builder response = ReturnResponse.newBuilder();

        String isbn = request.getIsbn();

        if (isInvalid(isbn)) {
            response.setIsSuccess(false)
                    .setError("missing field");

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        isbn = isbn.trim();

        Book book = library.get(isbn);

        if (book == null) {
            response.setIsSuccess(false)
                    .setError("book not found");

        } else if (!book.getIsBorrowed()) {
            response.setIsSuccess(false)
                    .setError("book is not borrowed");

        } else {

            Book updated = book.toBuilder()
                    .setIsBorrowed(false)
                    .setBorrowedBy("")
                    .setReturnBy("")
                    .build();

            library.put(isbn, updated);
            saveData();

            response.setIsSuccess(true)
                    .setMessage("book returned successfully");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // ===========================
    // LOAD DATA
    // ===========================
    private void loadData() {
        try {
            if (Files.exists(Paths.get(DATA_FILE))) {

                List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));

                for (String line : lines) {
                    String[] parts = line.split("\\|");

                    Book.Builder builder = Book.newBuilder()
                            .setTitle(parts[0])
                            .setAuthor(parts[1])
                            .setIsbn(parts[2])
                            .setIsBorrowed(Boolean.parseBoolean(parts[3]))
                            .setBorrowedBy(parts[4]);

                    builder.setReturnBy(parts.length > 5 ? parts[5] : "");

                    library.put(parts[2], builder.build());
                }

            } else {
                loadInitialBooks();
            }

        } catch (Exception e) {
            System.err.println("Error loading library: " + e.getMessage());
            loadInitialBooks();
        }
    }

    // ===========================
    // INITIAL DATA
    // ===========================
    private void loadInitialBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader(INIT_FILE))) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");

                Book book = Book.newBuilder()
                        .setTitle(parts[0])
                        .setAuthor(parts[1])
                        .setIsbn(parts[2])
                        .setIsBorrowed(false)
                        .setBorrowedBy("")
                        .setReturnBy("")
                        .build();

                library.put(parts[2], book);
            }

        } catch (Exception e) {
            System.err.println("Error loading initial books: " + e.getMessage());
        }
    }

    // ===========================
    // SAVE DATA
    // ===========================
    private void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE));
             PrintWriter initPw = new PrintWriter(new FileWriter(INIT_FILE))) {

            for (Book book : library.values()) {

                String line =
                        book.getTitle() + "|" +
                                book.getAuthor() + "|" +
                                book.getIsbn() + "|" +
                                book.getIsBorrowed() + "|" +
                                book.getBorrowedBy() + "|" +
                                book.getReturnBy();

                pw.println(line);

                // keep books.txt updated with base metadata only
                initPw.println(
                        book.getTitle() + "|" +
                                book.getAuthor() + "|" +
                                book.getIsbn()
                );
            }

        } catch (Exception e) {
            System.err.println("Error saving library: " + e.getMessage());
        }
    }
}