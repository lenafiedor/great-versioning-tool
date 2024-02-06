package uj.wmii.pwj.gvt;

public enum ExitCode {
    NO_ARGUMENTS(1, "Please specify command."),
    UNKNOWN_COMMAND(1, "Unknown command."),
    NOT_INITIALIZED(-2, "Current directory is not initialized. Please use init command to initialize."),
    IO_ERROR(-3, "Underlying system problem. See ERR for details."),

    INIT_ALREADY_INITIALIZED(10, "Current directory is already initialized."),
    INIT_SUCCESS(0, "Current directory initialized successfully."),

    VERSION_INVALID(60, "Invalid version number: "),
    VERSION_SUCCESS(0, "Version: "),

    ADD_NO_FILE_SPECIFIED(20, "Please specify file to add."),
    ADD_NON_EXISTING(21, "File not found. File: "),
    ADD_ALREADY_ADDED(0, "File already added. File: "),
    ADD_IO_ERROR(22, "File cannot be added. Se ERR for details. File: "),
    ADD_SUCCESS(0, "File added successfully. File: "),

    DETACH_NO_FILE_SPECIFIED(30, "Please specify file to detach."),
    DETACH_NON_EXISTING(0, "File is not added to gvt. File: "),
    DETACH_IO_ERROR(31, "File cannot be detached, see ERR for details. File: "),
    DETACH_SUCCESS(0, "File detached successfully. File: "),

    CHECKOUT_INVALID(60, "Invalid version number: "),
    CHECKOUT_SUCCESS(0, "Checkout successful for version: "),

    COMMIT_NO_FILE_SPECIFIED(50, "Please specify file to commit."),
    COMMIT_NOT_ADDED(0, "File is not added to gvt. File: "),
    COMMIT_NON_EXISTING(51, "File not found. File: "),
    COMMIT_IO_ERROR(52, "File cannot be committed, see ERR for details. File: "),
    COMMIT_SUCCESS(0, "File committed successfully. File: "),

    HISTORY_SUCCESS(0, "");


    private final int code;
    private final String description;

    ExitCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String message() {
        return description;
    }

    public int code() {
        return code;
    }
}
