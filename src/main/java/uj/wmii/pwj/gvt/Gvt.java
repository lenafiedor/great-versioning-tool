package uj.wmii.pwj.gvt;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Gvt {

    private final ExitHandler exitHandler;

    private static final Path PATH = Path.of(".gvt");
    private static final Path CURRENT_VERSION = PATH.resolve("current_version");
    private static final Path LAST_VERSION = PATH.resolve("last_version");
    private static final String VERSION_DETAILS = "version_details";

    public Gvt(ExitHandler exitHandler) {
        this.exitHandler = exitHandler;
    }

    private void exit(ExitCode ec) {
        exitHandler.exit(ec.code(), ec.message());
    }

    private void exit(ExitCode ec, String message) {
        exitHandler.exit(ec.code(), ec.message() + message);
    }

    private String getMessage(String... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                return args.length > i + 1 ? args[i + 1] : null;
            }
        }
        return null;
    }

    public static void main(String... args) {
        Gvt gvt = new Gvt(new ExitHandler());
        gvt.mainInternal(args);
    }

    void mainInternal(String... args) {
        if (args.length == 0) {
            exit(ExitCode.NO_ARGUMENTS);
        }
        else if (args[0].equals("init")) {
            if (Files.exists(PATH)) {
                exit(ExitCode.INIT_ALREADY_INITIALIZED);
            }
            else {
                init();
            }
        }
        else if (Files.notExists(PATH)) {
            exit(ExitCode.NOT_INITIALIZED);
        }
        else {
            switch (args[0]) {
                case "add":
                    if (args.length < 2) {
                        exit(ExitCode.ADD_NO_FILE_SPECIFIED);
                        return;
                    }
                    else {
                        add(Path.of(args[1]), getMessage(args));
                    }
                    break;
                case "detach":
                    if (args.length < 2) {
                        exit(ExitCode.DETACH_NO_FILE_SPECIFIED);
                        return;
                    }
                    else {
                        detach(Path.of(args[1]), getMessage(args));
                    }
                case "checkout":
                    if (args.length < 2) {
                        exit(ExitCode.CHECKOUT_INVALID);
                        return;
                    }
                    else {
                        checkout(args[1]);
                    }
                    break;
                case "commit":
                    if (args.length < 2) {
                        exit(ExitCode.COMMIT_NO_FILE_SPECIFIED);
                        return;
                    }
                    else {
                        commit(Path.of(args[1]), getMessage(args));
                    }
                    break;
                case "history":
                    if (args.length < 2) {
                        history(-1);
                    }
                    else {
                        history(Integer.parseInt(args[2]));
                    }
                    break;
                case "version":
                    if (args.length < 2) {
                        version(CURRENT_VERSION);
                    }
                    else {
                        version(PATH.resolve(args[1]));
                    }
                    break;
                default:
                    exit(ExitCode.UNKNOWN_COMMAND, args[0]);
            }
        }
    }

    private void init() {
        try {
            Files.createDirectory(PATH);
            Path zero = PATH.resolve("0");
            Files.createDirectory(zero);

            Path versionDetails = zero.resolve(VERSION_DETAILS);
            Files.createFile(versionDetails);
            Files.writeString(versionDetails, "GVT initialized.");

            Files.createFile(CURRENT_VERSION);
            Files.createFile(LAST_VERSION);
            Files.writeString(CURRENT_VERSION, "0");
            Files.writeString(LAST_VERSION, "0");

            exit(ExitCode.INIT_SUCCESS);
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.IO_ERROR);
        }
    }

    private void version(Path versionPath) {
        String version = getVersion(versionPath);
        if (version.isEmpty()) {
            version = getVersion(CURRENT_VERSION);
        }
        else if (version.length() > 1 || !Character.isDigit(version.charAt(0))) {
            exit(ExitCode.VERSION_INVALID, version);
        }
        exit(ExitCode.VERSION_SUCCESS, version + "\n" + getDetails(version));
    }

    private String getVersion(Path p) {
        try {
            return Files.readString(p).strip();
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.IO_ERROR);
            return "";
        }
    }

    private String getDetails(String version) {
        String versionDetailsPath = PATH.toString() + "/" + version + "/" + VERSION_DETAILS;
        try {
            return Files.readString(Path.of(versionDetailsPath));
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.IO_ERROR);
            return "";
        }
    }

    private void add(Path filePath, String message) {
        if (Files.notExists(filePath)) {
            exit(ExitCode.ADD_NON_EXISTING, filePath.toString());
            return;
        }
        try {
            String lastVersion = getVersion(LAST_VERSION);
            Path lastVersionPath = PATH.resolve(lastVersion).resolve(filePath);
            if (Files.exists(lastVersionPath)) {
                exit(ExitCode.ADD_ALREADY_ADDED, filePath.toString());
                return;
            }
            else {
                Path newVersionPath = createNewVersion(Integer.parseInt(lastVersion));
                Files.copy(filePath, newVersionPath.resolve(filePath), StandardCopyOption.REPLACE_EXISTING);
                if (message == null) {
                    message = ExitCode.ADD_SUCCESS.message() + filePath;
                }
                completeVersionDetails(newVersionPath, message);
                exit(ExitCode.ADD_SUCCESS, filePath.toString());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.ADD_IO_ERROR, filePath.toString());
        }
    }

    private Path createNewVersion(int versionNumber) {
        try {
            Path currentVersionPath = PATH.resolve(String.valueOf(versionNumber));
            Path newVersionPath = PATH.resolve(String.valueOf(versionNumber + 1));
            Files.createDirectory(newVersionPath);
            copyDirectory(currentVersionPath, newVersionPath);
            return newVersionPath;
        }
        catch (IOException e) {
            e.printStackTrace();
            return Path.of("");
        }
    }

    private static void copyDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = targetDirectory.resolve(sourceDirectory.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetDirectory.resolve(sourceDirectory.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void completeVersionDetails(Path newVersionPath, String message) {
        try {
            Path versionDetails = newVersionPath.resolve(VERSION_DETAILS);
            Files.writeString(versionDetails, message);

            int version = Integer.parseInt(getVersion(CURRENT_VERSION)) + 1;
            Files.writeString(CURRENT_VERSION, String.valueOf(version));
            Files.writeString(LAST_VERSION, String.valueOf(version));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void detach(Path filePath, String message) {
        String currentVersion = PATH.toString() + "/" + getVersion(LAST_VERSION);
        Path fullPath = Path.of(currentVersion).resolve(filePath);
        if (Files.notExists(fullPath)) {
            exit(ExitCode.DETACH_NON_EXISTING, filePath.toString());
            return;
        }
        try {
            Path newVersionPath = createNewVersion(Integer.parseInt(getVersion(LAST_VERSION)));
            Files.delete(newVersionPath.resolve(filePath));
            if (message == null) {
                message = ExitCode.DETACH_SUCCESS.message() + filePath;
            }
            completeVersionDetails(newVersionPath, message);
            exit(ExitCode.DETACH_SUCCESS, filePath.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.ADD_IO_ERROR, filePath.toString());
        }
    }

    private void checkout(String checkoutVersion) {
        int version = -1;
        try {
            version = Integer.parseInt(checkoutVersion);
        }
        catch (NumberFormatException e) {
            exit(ExitCode.CHECKOUT_INVALID, checkoutVersion);
        }
        try {
            int lastVersion = Integer.parseInt(Files.readString(LAST_VERSION));
            if (version > lastVersion || version < 0) {
                exit(ExitCode.CHECKOUT_INVALID, checkoutVersion);
                return;
            }
            copyDirectory(PATH.resolve(String.valueOf(version)), Path.of(""));
            exit(ExitCode.CHECKOUT_SUCCESS, checkoutVersion);
        }
        catch (IOException e) {
            exit(ExitCode.IO_ERROR);
        }
    }

    private void commit(Path fileToCommit, String message) {
        if (Files.notExists(fileToCommit)) {
            exit(ExitCode.COMMIT_NON_EXISTING, fileToCommit.toString());
        }
        try {
            String lastVersion = getVersion(LAST_VERSION);
            Path lastVersionPath = PATH.resolve(lastVersion).resolve(fileToCommit);
            if (Files.notExists(lastVersionPath)) {
                exit(ExitCode.COMMIT_NOT_ADDED, fileToCommit.toString());
                return;
            }
            Path newVersionPath = createNewVersion(Integer.parseInt(lastVersion));
            Files.copy(fileToCommit, newVersionPath.resolve(fileToCommit), StandardCopyOption.REPLACE_EXISTING);
            if (message == null) {
                message = ExitCode.COMMIT_SUCCESS.message() + fileToCommit;
            }
            completeVersionDetails(newVersionPath, message);
            exit(ExitCode.COMMIT_SUCCESS, fileToCommit.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(ExitCode.COMMIT_IO_ERROR, fileToCommit.toString());
        }
    }

    private void history(int nVersions) {
        int lastVersion = Integer.parseInt(getVersion(LAST_VERSION));
        if (nVersions <=0 || nVersions > lastVersion) {
            nVersions = lastVersion + 1;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = lastVersion; i > lastVersion - nVersions; i--) {
            String versionMessage = getDetails(String.valueOf(i));
            String firstLine = versionMessage.split("\n")[0];
            sb.append(i).append(": ").append(firstLine).append("\n");
        }
        exit(ExitCode.HISTORY_SUCCESS, sb.toString());
    }
}
