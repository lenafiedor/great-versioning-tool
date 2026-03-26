## Great Versioning Tool (GVT)

File version control system — a (very) simplified GIT.

The system works only with files. It does not support subdirectories, and links.

The basic unit of operation is a *version*. Each *version* contains:

- Version number (from `0` to `Integer.MAX_VALUE`).
- Commit message that was added when the version was committed (`commit`).
- All files that were added (using the `add` command) to GVT.

Files committed in a given version cannot be modified within that version — committing such a modification means creating a new version.

### Running the application

- The system provides an executable `Gvt` class. This class will be used to run all commands.
- The command should always be the first argument passed to the program.
- All commands except `init` work only in an initialized directory.

### Rules for commands: `add`, `detach`, `commit`

These commands have an optional parameter `-m {"User message in quotes"}`, which may be provided as the last parameter.

### Commands

- `init`: Initializes the GVT system in the current directory and sets both the active and latest version to `0`.
- `add`: Adds the filesas a parameter to the latest version. It accepts an optional user message.
- `detach`: Detaches the file specified as a parameter from the latest version. It accepts an optional user message.
- `checkout`: Restores files to the state from a specific version provided as a parameter. This command does not change the tracking state of files in GVT.
- `commit`: Creates a new version in GVT using the file specified as a parameter. It accepts an optional user message.
- `history`: Diplays the version history. `-last n` parameter can be used to disply only n last version.
- `version`: Displays the details of the version specified as a parameter. When no parameter is provided, the details of the latest version are displayed.
