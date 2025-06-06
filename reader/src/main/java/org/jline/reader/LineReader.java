/*
 * Copyright (c) 2002-2025, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.reader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.IntConsumer;

import org.jline.keymap.KeyMap;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

/** Read lines from the console, with input editing.
 *
 * <h2>Thread safety</h2>
 * The <code>LineReader</code> implementations are not thread safe,
 * thus you should not attempt to use a single reader in several threads.
 * Any attempt to call one of the <code>readLine</code> call while one is
 * already executing in a different thread will immediately result in an
 * <code>IllegalStateException</code> being thrown.  Other calls may lead to
 * unknown behaviors. There is one exception though: users are allowed to call
 * {@link #printAbove(String)} or {@link #printAbove(AttributedString)} at
 * any time to allow text to be printed above the current prompt.
 *
 * <h2>Prompt strings</h2>
 * It is traditional for an interactive console-based program
 * to print a short prompt string to signal that the user is expected
 * to type a command.  JLine supports 3 kinds of prompt string:
 * <ul>
 * <li> The normal prompt at the start (left) of the initial line of a command.
 * <li> An optional right prompt at the right border of the initial line.
 * <li> A start (left) prompt for continuation lines.  I.e. the lines
 * after the first line of a multi-line command.
 * </ul>
 * <p>
 * All of these are specified with prompt templates,
 * which are similar to {@code printf} format strings,
 * using the character {@code '%'} to indicate special functionality.
 * </p>
 * The pattern may include ANSI escapes.
 * It may include these template markers:
 * <dl>
 * <dt>{@code %N}</dt>
 * <dd>A line number. This is the sum of {@code getLineNumber()}
 *   and a counter starting with 1 for the first continuation line.
 * </dd>
 * <dt>{@code %M}</dt>
 * <dd>A short word explaining what is "missing". This is supplied from
 * the {@link EOFError#getMissing()} method, if provided.
 * Defaults to an empty string.
 * </dd>
 * <dt>{@code %}<var>n</var>{@code P}<var>c</var></dt>
 * <dd>Insert padding at this position, repeating the following
 *   character <var>c</var> as needed to bring the total prompt
 *   column width as specified by the digits <var>n</var>.
 * </dd>
 * <dt>{@code %P}<var>c</var></dt>
 * <dd>As before, but use width from the initial prompt.
 * </dd>
 * <dt>{@code %%}</dt>
 * <dd>A literal {@code '%'}.
 * </dd>
 * <dt><code>%{</code></dt><dt><code>%}</code></dt>
 * <dd>Text between a <code>%{</code>...<code>%}</code> pair is printed as
 * part of a prompt, but not interpreted by JLine
 * (except that {@code '%'}-escapes are processed).  The text is assumed
 * to take zero columns (not move the cursor).  If it changes the style,
 * you're responsible for changing it back.  Standard ANSI escape sequences
 * do not need to be within a <code>%{</code>...<code>%}</code> pair
 * (though can be) since JLine knows how to deal with them.  However,
 * these delimiters are needed for unusual non-standard escape sequences.
 * </dd>
 * </dl>
 */
public interface LineReader {

    /**
     * System property that can be set to avoid a warning being logged
     * when using a Parser which does not return {@link CompletingParsedLine} objects.
     */
    String PROP_SUPPORT_PARSEDLINE = "org.jline.reader.support.parsedline";

    //
    // Widget names
    //
    String CALLBACK_INIT = "callback-init";
    String CALLBACK_FINISH = "callback-finish";
    String CALLBACK_KEYMAP = "callback-keymap";

    String ACCEPT_AND_INFER_NEXT_HISTORY = "accept-and-infer-next-history";
    String ACCEPT_AND_HOLD = "accept-and-hold";
    String ACCEPT_LINE = "accept-line";
    String ACCEPT_LINE_AND_DOWN_HISTORY = "accept-line-and-down-history";
    String ARGUMENT_BASE = "argument-base";
    String BACKWARD_CHAR = "backward-char";
    String BACKWARD_DELETE_CHAR = "backward-delete-char";
    String BACKWARD_DELETE_WORD = "backward-delete-word";
    String BACKWARD_KILL_LINE = "backward-kill-line";
    String BACKWARD_KILL_WORD = "backward-kill-word";
    String BACKWARD_WORD = "backward-word";
    String BEEP = "beep";
    String BEGINNING_OF_BUFFER_OR_HISTORY = "beginning-of-buffer-or-history";
    String BEGINNING_OF_HISTORY = "beginning-of-history";
    String BEGINNING_OF_LINE = "beginning-of-line";
    String BEGINNING_OF_LINE_HIST = "beginning-of-line-hist";
    String CAPITALIZE_WORD = "capitalize-word";
    String CHARACTER_SEARCH = "character-search";
    String CHARACTER_SEARCH_BACKWARD = "character-search-backward";
    String CLEAR = "clear";
    String CLEAR_SCREEN = "clear-screen";
    String COMPLETE_PREFIX = "complete-prefix";
    String COMPLETE_WORD = "complete-word";
    String COPY_PREV_WORD = "copy-prev-word";
    String COPY_REGION_AS_KILL = "copy-region-as-kill";
    String DELETE_CHAR = "delete-char";
    String DELETE_CHAR_OR_LIST = "delete-char-or-list";
    String DELETE_WORD = "delete-word";
    String DIGIT_ARGUMENT = "digit-argument";
    String DO_LOWERCASE_VERSION = "do-lowercase-version";
    String DOWN_CASE_WORD = "down-case-word";
    String DOWN_HISTORY = "down-history";
    String DOWN_LINE = "down-line";
    String DOWN_LINE_OR_HISTORY = "down-line-or-history";
    String DOWN_LINE_OR_SEARCH = "down-line-or-search";
    String EDIT_AND_EXECUTE_COMMAND = "edit-and-execute-command";
    String EMACS_BACKWARD_WORD = "emacs-backward-word";
    String EMACS_EDITING_MODE = "emacs-editing-mode";
    String EMACS_FORWARD_WORD = "emacs-forward-word";
    String END_OF_BUFFER_OR_HISTORY = "end-of-buffer-or-history";
    String END_OF_HISTORY = "end-of-history";
    String END_OF_LINE = "end-of-line";
    String END_OF_LINE_HIST = "end-of-line-hist";
    String EXCHANGE_POINT_AND_MARK = "exchange-point-and-mark";
    String EXECUTE_NAMED_CMD = "execute-named-cmd";
    String EXPAND_HISTORY = "expand-history";
    String EXPAND_OR_COMPLETE = "expand-or-complete";
    String EXPAND_OR_COMPLETE_PREFIX = "expand-or-complete-prefix";
    String EXPAND_WORD = "expand-word";
    String FRESH_LINE = "fresh-line";
    String FORWARD_CHAR = "forward-char";
    String FORWARD_WORD = "forward-word";
    String HISTORY_BEGINNING_SEARCH_BACKWARD = "history-beginning-search-backward";
    String HISTORY_BEGINNING_SEARCH_FORWARD = "history-beginning-search-forward";
    String HISTORY_INCREMENTAL_PATTERN_SEARCH_BACKWARD = "history-incremental-pattern-search-backward";
    String HISTORY_INCREMENTAL_PATTERN_SEARCH_FORWARD = "history-incremental-pattern-search-forward";
    String HISTORY_INCREMENTAL_SEARCH_BACKWARD = "history-incremental-search-backward";
    String HISTORY_INCREMENTAL_SEARCH_FORWARD = "history-incremental-search-forward";
    String HISTORY_SEARCH_BACKWARD = "history-search-backward";
    String HISTORY_SEARCH_FORWARD = "history-search-forward";
    String INSERT_CLOSE_CURLY = "insert-close-curly";
    String INSERT_CLOSE_PAREN = "insert-close-paren";
    String INSERT_CLOSE_SQUARE = "insert-close-square";
    String INFER_NEXT_HISTORY = "infer-next-history";
    String INSERT_COMMENT = "insert-comment";
    String INSERT_LAST_WORD = "insert-last-word";
    String KILL_BUFFER = "kill-buffer";
    String KILL_LINE = "kill-line";
    String KILL_REGION = "kill-region";
    String KILL_WHOLE_LINE = "kill-whole-line";
    String KILL_WORD = "kill-word";
    String LIST_CHOICES = "list-choices";
    String LIST_EXPAND = "list-expand";
    String MAGIC_SPACE = "magic-space";
    String MENU_EXPAND_OR_COMPLETE = "menu-expand-or-complete";
    String MENU_COMPLETE = "menu-complete";
    String MENU_SELECT = "menu-select";
    String NEG_ARGUMENT = "neg-argument";
    String OVERWRITE_MODE = "overwrite-mode";
    String PUT_REPLACE_SELECTION = "put-replace-selection";
    String QUOTED_INSERT = "quoted-insert";
    String READ_COMMAND = "read-command";
    String RECURSIVE_EDIT = "recursive-edit";
    String REDISPLAY = "redisplay";
    String REDRAW_LINE = "redraw-line";
    String REDO = "redo";
    String REVERSE_MENU_COMPLETE = "reverse-menu-complete";
    String SELF_INSERT = "self-insert";
    String SELF_INSERT_UNMETA = "self-insert-unmeta";
    String SEND_BREAK = "abort";
    String SET_LOCAL_HISTORY = "set-local-history";
    String SET_MARK_COMMAND = "set-mark-command";
    String SPELL_WORD = "spell-word";
    String SPLIT_UNDO = "split-undo";
    String TRANSPOSE_CHARS = "transpose-chars";
    String TRANSPOSE_WORDS = "transpose-words";
    String UNDEFINED_KEY = "undefined-key";
    String UNDO = "undo";
    String UNIVERSAL_ARGUMENT = "universal-argument";
    String UP_CASE_WORD = "up-case-word";
    String UP_HISTORY = "up-history";
    String UP_LINE = "up-line";
    String UP_LINE_OR_HISTORY = "up-line-or-history";
    String UP_LINE_OR_SEARCH = "up-line-or-search";
    String VI_ADD_EOL = "vi-add-eol";
    String VI_ADD_NEXT = "vi-add-next";
    String VI_BACKWARD_BLANK_WORD = "vi-backward-blank-word";
    String VI_BACKWARD_BLANK_WORD_END = "vi-backward-blank-word-end";
    String VI_BACKWARD_CHAR = "vi-backward-char";
    String VI_BACKWARD_DELETE_CHAR = "vi-backward-delete-char";
    String VI_BACKWARD_KILL_WORD = "vi-backward-kill-word";
    String VI_BACKWARD_WORD = "vi-backward-word";
    String VI_BACKWARD_WORD_END = "vi-backward-word-end";
    String VI_BEGINNING_OF_LINE = "vi-beginning-of-line";
    String VI_CHANGE = "vi-change-to";
    String VI_CHANGE_EOL = "vi-change-eol";
    String VI_CHANGE_WHOLE_LINE = "vi-change-whole-line";
    String VI_CMD_MODE = "vi-cmd-mode";
    String VI_DELETE = "vi-delete";
    String VI_DELETE_CHAR = "vi-delete-char";
    String VI_DIGIT_OR_BEGINNING_OF_LINE = "vi-digit-or-beginning-of-line";
    String VI_DOWN_LINE_OR_HISTORY = "vi-down-line-or-history";
    String VI_END_OF_LINE = "vi-end-of-line";
    String VI_FETCH_HISTORY = "vi-fetch-history";
    String VI_FIND_NEXT_CHAR = "vi-find-next-char";
    String VI_FIND_NEXT_CHAR_SKIP = "vi-find-next-char-skip";
    String VI_FIND_PREV_CHAR = "vi-find-prev-char";
    String VI_FIND_PREV_CHAR_SKIP = "vi-find-prev-char-skip";
    String VI_FIRST_NON_BLANK = "vi-first-non-blank";
    String VI_FORWARD_BLANK_WORD = "vi-forward-blank-word";
    String VI_FORWARD_BLANK_WORD_END = "vi-forward-blank-word-end";
    String VI_FORWARD_CHAR = "vi-forward-char";
    String VI_FORWARD_WORD = "vi-forward-word";
    String VI_FORWARD_WORD_END = "vi-forward-word-end";
    String VI_GOTO_COLUMN = "vi-goto-column";
    String VI_HISTORY_SEARCH_BACKWARD = "vi-history-search-backward";
    String VI_HISTORY_SEARCH_FORWARD = "vi-history-search-forward";
    String VI_INSERT = "vi-insert";
    String VI_INSERT_BOL = "vi-insert-bol";
    String VI_INSERT_COMMENT = "vi-insert-comment";
    String VI_JOIN = "vi-join";
    String VI_KILL_EOL = "vi-kill-eol";
    String VI_KILL_LINE = "vi-kill-line";
    String VI_MATCH_BRACKET = "vi-match-bracket";
    String VI_OPEN_LINE_ABOVE = "vi-open-line-above";
    String VI_OPEN_LINE_BELOW = "vi-open-line-below";
    String VI_OPER_SWAP_CASE = "vi-oper-swap-case";
    String VI_PUT_AFTER = "vi-put-after";
    String VI_PUT_BEFORE = "vi-put-before";
    String VI_QUOTED_INSERT = "vi-quoted-insert";
    String VI_REPEAT_CHANGE = "vi-repeat-change";
    String VI_REPEAT_FIND = "vi-repeat-find";
    String VI_REPEAT_SEARCH = "vi-repeat-search";
    String VI_REPLACE = "vi-replace";
    String VI_REPLACE_CHARS = "vi-replace-chars";
    String VI_REV_REPEAT_FIND = "vi-rev-repeat-find";
    String VI_REV_REPEAT_SEARCH = "vi-rev-repeat-search";
    String VI_SET_BUFFER = "vi-set-buffer";
    String VI_SUBSTITUTE = "vi-substitute";
    String VI_SWAP_CASE = "vi-swap-case";
    String VI_UNDO_CHANGE = "vi-undo-change";
    String VI_UP_LINE_OR_HISTORY = "vi-up-line-or-history";
    String VI_YANK = "vi-yank";
    String VI_YANK_EOL = "vi-yank-eol";
    String VI_YANK_WHOLE_LINE = "vi-yank-whole-line";
    String VISUAL_LINE_MODE = "visual-line-mode";
    String VISUAL_MODE = "visual-mode";
    String WHAT_CURSOR_POSITION = "what-cursor-position";
    String YANK = "yank";
    String YANK_POP = "yank-pop";
    String MOUSE = "mouse";
    String FOCUS_IN = "terminal-focus-in";
    String FOCUS_OUT = "terminal-focus-out";

    String BEGIN_PASTE = "begin-paste";

    //
    // KeyMap names
    //

    String VICMD = "vicmd";
    String VIINS = "viins";
    String VIOPP = "viopp";
    String VISUAL = "visual";
    String MAIN = "main";
    String EMACS = "emacs";
    String SAFE = ".safe";
    String DUMB = "dumb";
    String MENU = "menu";

    //
    // Variable names
    //

    String BIND_TTY_SPECIAL_CHARS = "bind-tty-special-chars";
    String COMMENT_BEGIN = "comment-begin";
    String BELL_STYLE = "bell-style";
    String PREFER_VISIBLE_BELL = "prefer-visible-bell";
    /** tab completion: if candidates are more than list-max a question will be asked before displaying them */
    String LIST_MAX = "list-max";
    /**
     * tab completion: if candidates are less than menu-list-max
     * they are displayed in a list below the field to be completed
     */
    String MENU_LIST_MAX = "menu-list-max";

    String DISABLE_HISTORY = "disable-history";
    String DISABLE_COMPLETION = "disable-completion";
    String EDITING_MODE = "editing-mode";
    String KEYMAP = "keymap";
    String BLINK_MATCHING_PAREN = "blink-matching-paren";
    String WORDCHARS = "WORDCHARS";
    String REMOVE_SUFFIX_CHARS = "REMOVE_SUFFIX_CHARS";
    String SEARCH_TERMINATORS = "search-terminators";
    /** Number of matching errors that are accepted by the completion matcher */
    String ERRORS = "errors";
    /** Property for the "others" group name */
    String OTHERS_GROUP_NAME = "OTHERS_GROUP_NAME";
    /** Property for the "original" group name */
    String ORIGINAL_GROUP_NAME = "ORIGINAL_GROUP_NAME";
    /** Completion style for displaying groups name */
    String COMPLETION_STYLE_GROUP = "COMPLETION_STYLE_GROUP";

    String COMPLETION_STYLE_LIST_GROUP = "COMPLETION_STYLE_LIST_GROUP";
    /** Completion style for displaying the current selected item */
    String COMPLETION_STYLE_SELECTION = "COMPLETION_STYLE_SELECTION";

    String COMPLETION_STYLE_LIST_SELECTION = "COMPLETION_STYLE_LIST_SELECTION";
    /** Completion style for displaying the candidate description */
    String COMPLETION_STYLE_DESCRIPTION = "COMPLETION_STYLE_DESCRIPTION";

    String COMPLETION_STYLE_LIST_DESCRIPTION = "COMPLETION_STYLE_LIST_DESCRIPTION";
    /** Completion style for displaying the matching part of candidates */
    String COMPLETION_STYLE_STARTING = "COMPLETION_STYLE_STARTING";

    String COMPLETION_STYLE_LIST_STARTING = "COMPLETION_STYLE_LIST_STARTING";
    /** Completion style for displaying the list */
    String COMPLETION_STYLE_BACKGROUND = "COMPLETION_STYLE_BACKGROUND";

    String COMPLETION_STYLE_LIST_BACKGROUND = "COMPLETION_STYLE_LIST_BACKGROUND";
    /**
     * Set the template for prompts for secondary (continuation) lines.
     * This is a prompt template as described in the class header.
     */
    String SECONDARY_PROMPT_PATTERN = "secondary-prompt-pattern";
    /**
     * When in multiline edit mode, this variable can be used
     * to offset the line number displayed.
     */
    String LINE_OFFSET = "line-offset";

    /**
     * Timeout for ambiguous key sequences.
     * If the key sequence is ambiguous, i.e. there is a matching
     * sequence but the sequence is also a prefix for other bindings,
     * the next key press will be waited for a specified amount of
     * time.  If the timeout elapses, the matched sequence will be
     * used.
     */
    String AMBIGUOUS_BINDING = "ambiguous-binding";

    /**
     * Colon separated list of patterns that will not be saved in history.
     */
    String HISTORY_IGNORE = "history-ignore";

    /**
     * File system history path.
     */
    String HISTORY_FILE = "history-file";

    /**
     * Number of history items to keep in memory.
     */
    String HISTORY_SIZE = "history-size";

    /**
     * Number of history items to keep in the history file.
     */
    String HISTORY_FILE_SIZE = "history-file-size";

    /**
     * New line automatic indentation after opening/closing bracket.
     */
    String INDENTATION = "indentation";

    /**
     * Max buffer size for advanced features.
     * Once the length of the buffer reaches this threshold, no
     * advanced features will be enabled. This includes the undo
     * buffer, syntax highlighting, parsing, etc....
     */
    String FEATURES_MAX_BUFFER_SIZE = "features-max-buffer-size";

    /**
     * Min buffer size for tab auto-suggestions.
     * For shorter buffer sizes auto-suggestions are not resolved.
     */
    String SUGGESTIONS_MIN_BUFFER_SIZE = "suggestions-min-buffer-size";

    /**
     * Max number of times a command can be repeated.
     */
    String MAX_REPEAT_COUNT = "max-repeat-count";

    /**
     * Number of spaces to display a tabulation, the default is 4.
     */
    String TAB_WIDTH = "tab-width";

    /**
     * Name of inputrc to read at line reader creation time.
     */
    String INPUT_RC_FILE_NAME = "input-rc-file-name";

    /**
     * Prefix to automatically delegate variables to system properties
     */
    String SYSTEM_PROPERTY_PREFIX = "system-property-prefix";

    /**
     * Returns the default key maps used by the LineReader.
     * <p>
     * These key maps define the standard key bindings for different editing modes
     * such as Emacs mode, Vi command mode, Vi insert mode, etc.
     *
     * @return a map of key map names to key maps
     */
    Map<String, KeyMap<Binding>> defaultKeyMaps();

    enum Option {
        COMPLETE_IN_WORD,
        /** use camel case completion matcher */
        COMPLETE_MATCHER_CAMELCASE,
        /** use type completion matcher */
        COMPLETE_MATCHER_TYPO(true),
        /** disable special handling of magic history expansion commands like "!" and "!!" and "!n" and "!-n" and "!string" and "^string1^string2", as well as [interpret escape characters](https://github.com/jline/jline3/issues/1238) **/
        DISABLE_EVENT_EXPANSION,
        HISTORY_VERIFY,
        HISTORY_IGNORE_SPACE(true),
        HISTORY_IGNORE_DUPS(true),
        HISTORY_REDUCE_BLANKS(true),
        HISTORY_BEEP(true),
        HISTORY_INCREMENTAL(true),
        HISTORY_TIMESTAMPED(true),
        /** when displaying candidates, group them by {@link Candidate#group()} */
        AUTO_GROUP(true),
        AUTO_MENU(true),
        AUTO_LIST(true),
        /** list candidates below the field to be completed */
        AUTO_MENU_LIST,
        RECOGNIZE_EXACT,
        /** display group name before each group (else display all group names first) */
        GROUP(true),
        /** when double tab to select candidate keep candidates grouped (else loose grouping) */
        GROUP_PERSIST,
        /** if completion is case insensitive or not */
        CASE_INSENSITIVE,
        LIST_AMBIGUOUS,
        LIST_PACKED,
        LIST_ROWS_FIRST,
        GLOB_COMPLETE,
        MENU_COMPLETE,
        /** if set and not at start of line before prompt, move to new line */
        AUTO_FRESH_LINE,

        /** After writing into the rightmost column, do we immediately
         * move to the next line (the default)? Or do we wait until
         * the next character.
         * If set, an input line that is exactly {@code N*columns} wide will
         * use {@code N} screen lines; otherwise it will use {@code N+1} lines.
         * When the cursor position is the right margin of the last line
         * (i.e. after {@code N*columns} normal characters), if this option
         * it set, the cursor will be remain on the last line (line {@code N-1},
         * zero-origin); if unset the cursor will be on the empty next line.
         * Regardless, for all except the last screen line if the cursor is at
         * the right margin, it will be shown at the start of the next line.
         */
        DELAY_LINE_WRAP,
        AUTO_PARAM_SLASH(true),
        AUTO_REMOVE_SLASH(true),
        /** FileNameCompleter: Use '/' character as a file directory separator */
        USE_FORWARD_SLASH,
        /** When hitting the <code>&lt;tab&gt;</code> key at the beginning of the line, insert a tabulation
         *  instead of completing.  This is mainly useful when {@link #BRACKETED_PASTE} is
         *  disabled, so that copy/paste of indented text does not trigger completion.
         */
        INSERT_TAB,
        MOUSE,
        DISABLE_HIGHLIGHTER,
        BRACKETED_PASTE(true),
        /**
         * Instead of printing a new line when the line is read, the entire line
         * (including the prompt) will be erased, thereby leaving the screen as it
         * was before the readLine call.
         */
        ERASE_LINE_ON_FINISH,

        /** if history search is fully case insensitive */
        CASE_INSENSITIVE_SEARCH,

        /** Automatic insertion of closing bracket */
        INSERT_BRACKET,

        /** Show command options tab completion candidates for zero length word */
        EMPTY_WORD_OPTIONS(true),

        /** Disable the undo feature */
        DISABLE_UNDO;

        private final boolean def;

        Option() {
            this(false);
        }

        Option(boolean def) {
            this.def = def;
        }

        public final boolean isSet(Map<Option, Boolean> options) {
            Boolean b = options.get(this);
            return b != null ? b : this.isDef();
        }

        public boolean isDef() {
            return def;
        }
    }

    enum RegionType {
        NONE,
        CHAR,
        LINE,
        PASTE
    }

    enum SuggestionType {
        /**
         * As you type command line suggestions are disabled.
         */
        NONE,
        /**
         * Prepare command line suggestions using command history.
         * Requires an additional widgets implementation.
         */
        HISTORY,
        /**
         * Prepare command line suggestions using command completer data.
         */
        COMPLETER,
        /**
         * Prepare command line suggestions using command completer data and/or command positional argument descriptions.
         * Requires an additional widgets implementation.
         */
        TAIL_TIP
    }

    /**
     * Read the next line and return the contents of the buffer.
     *
     * Equivalent to <code>readLine(null, null, null)</code>.
     *
     * @return the line read
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine() throws UserInterruptException, EndOfFileException;

    /**
     * Read the next line with the specified character mask. If null, then
     * characters will be echoed. If 0, then no characters will be echoed.
     *
     * Equivalent to <code>readLine(null, mask, null)</code>
     *
     * @param mask      The mask character, <code>null</code> or <code>0</code>.
     * @return          A line that is read from the terminal, can never be null.
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(Character mask) throws UserInterruptException, EndOfFileException;

    /**
     * Read the next line with the specified prompt.
     * If null, then the default prompt will be used.
     *
     * Equivalent to <code>readLine(prompt, null, null)</code>
     *
     * @param prompt    The prompt to issue to the terminal, may be null.
     * @return          A line that is read from the terminal, can never be null.
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(String prompt) throws UserInterruptException, EndOfFileException;

    /**
     * Read a line from the <i>in</i> {@link InputStream}, and return the line
     * (without any trailing newlines).
     *
     * Equivalent to <code>readLine(prompt, mask, null)</code>
     *
     * @param prompt    The prompt to issue to the terminal, may be null.
     * @param mask      The mask character, <code>null</code> or <code>0</code>.
     * @return          A line that is read from the terminal, can never be null.
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(String prompt, Character mask) throws UserInterruptException, EndOfFileException;

    /**
     * Read a line from the <i>in</i> {@link InputStream}, and return the line
     * (without any trailing newlines).
     *
     * Equivalent to <code>readLine(prompt, null, mask, buffer)</code>
     *
     * @param prompt    The prompt to issue to the terminal, may be null.
     *   This is a template, with optional {@code '%'} escapes, as
     *   described in the class header.
     * @param mask      The character mask, may be null.
     * @param buffer    The default value presented to the user to edit, may be null.
     * @return          A line that is read from the terminal, can never be null.
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(String prompt, Character mask, String buffer) throws UserInterruptException, EndOfFileException;

    /**
     * Read a line from the <i>in</i> {@link InputStream}, and return the line
     * (without any trailing newlines).
     *
     * @param prompt      The prompt to issue to the terminal, may be null.
     *   This is a template, with optional {@code '%'} escapes, as
     *   described in the class header.
     * @param rightPrompt The right prompt
     *   This is a template, with optional {@code '%'} escapes, as
     *   described in the class header.
     * @param mask        The character mask, may be null.
     * @param buffer      The default value presented to the user to edit, may be null.
     * @return            A line that is read from the terminal, can never be null.
     *
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(String prompt, String rightPrompt, Character mask, String buffer)
            throws UserInterruptException, EndOfFileException;

    /**
     * Read a line from the <i>in</i> {@link InputStream}, and return the line
     * (without any trailing newlines).
     *
     * @param prompt      The prompt to issue to the terminal, may be null.
     *   This is a template, with optional {@code '%'} escapes, as
     *   described in the class header.
     * @param rightPrompt The right prompt
     *   This is a template, with optional {@code '%'} escapes, as
     *   described in the class header.
     * @param maskingCallback  The {@link MaskingCallback} to use when displaying lines and adding them to the line {@link History}
     * @param buffer      The default value presented to the user to edit, may be null.
     * @return            A line that is read from the terminal, can never be null.
     *
     * @throws UserInterruptException if readLine was interrupted (using Ctrl-C for example)
     * @throws EndOfFileException if an EOF has been found (using Ctrl-D for example)
     * @throws java.io.IOError in case of other i/o errors
     */
    String readLine(String prompt, String rightPrompt, MaskingCallback maskingCallback, String buffer)
            throws UserInterruptException, EndOfFileException;

    /**
     * Prints a line above the prompt and redraw everything.
     * If the LineReader is not actually reading a line, the string will simply be printed to the terminal.
     *
     * @see #printAbove(AttributedString)
     * @param str the string to print
     */
    void printAbove(String str);

    /**
     * Prints a string before the prompt and redraw everything.
     * If the LineReader is not actually reading a line, the string will simply be printed to the terminal.
     *
     * @see #printAbove(String)
     * @param str the string to print
     */
    void printAbove(AttributedString str);

    /**
     * Check if a thread is currently in a <code>readLine()</code> call.
     *
     * @return <code>true</code> if there is an ongoing <code>readLine()</code> call.
     */
    boolean isReading();

    //
    // Chainable setters
    //

    /**
     * Sets a variable in the LineReader and returns the LineReader for method chaining.
     * <p>
     * Variables control various aspects of the LineReader's behavior. See the
     * various variable constants defined in this interface for available options.
     *
     * @param name the variable name
     * @param value the variable value
     * @return this LineReader
     */
    LineReader variable(String name, Object value);

    /**
     * Sets an option in the LineReader and returns the LineReader for method chaining.
     * <p>
     * Options control various aspects of the LineReader's behavior. See the
     * {@link Option} enum for available options.
     *
     * @param option the option to set
     * @param value the option value
     * @return this LineReader
     */
    LineReader option(Option option, boolean value);

    /**
     * Calls a widget by name.
     * <p>
     * Widgets are functions that perform editing operations. This method allows
     * invoking a widget programmatically rather than through a key binding.
     *
     * @param name the name of the widget to call
     */
    void callWidget(String name);

    /**
     * Returns a map of all variables set in the LineReader.
     * <p>
     * Variables control various aspects of the LineReader's behavior. See the
     * various variable constants defined in this interface for available options.
     *
     * @return a map of variable names to their values
     */
    Map<String, Object> getVariables();

    /**
     * Returns the value of a variable.
     * <p>
     * Variables control various aspects of the LineReader's behavior. See the
     * various variable constants defined in this interface for available options.
     *
     * @param name the variable name
     * @return the variable value, or null if the variable is not set
     */
    Object getVariable(String name);

    /**
     * Sets a variable in the LineReader.
     * <p>
     * Variables control various aspects of the LineReader's behavior. See the
     * various variable constants defined in this interface for available options.
     *
     * @param name the variable name
     * @param value the variable value
     */
    void setVariable(String name, Object value);

    /**
     * Checks if an option is set.
     * <p>
     * Options control various aspects of the LineReader's behavior. See the
     * {@link Option} enum for available options.
     *
     * @param option the option to check
     * @return true if the option is set, false otherwise
     */
    boolean isSet(Option option);

    /**
     * Sets an option to true.
     * <p>
     * Options control various aspects of the LineReader's behavior. See the
     * {@link Option} enum for available options.
     *
     * @param option the option to set
     */
    void setOpt(Option option);

    /**
     * Sets an option to false.
     * <p>
     * Options control various aspects of the LineReader's behavior. See the
     * {@link Option} enum for available options.
     *
     * @param option the option to unset
     */
    void unsetOpt(Option option);

    /**
     * Returns the terminal associated with this LineReader.
     * <p>
     * The terminal is used for input/output operations and provides information
     * about the terminal capabilities and size.
     *
     * @return the terminal
     */
    Terminal getTerminal();

    /**
     * Returns a map of all widgets registered with this LineReader.
     * <p>
     * Widgets are functions that perform editing operations and can be bound
     * to key sequences.
     *
     * @return a map of widget names to widgets
     */
    Map<String, Widget> getWidgets();

    /**
     * Returns a map of all built-in widgets provided by the LineReader.
     * <p>
     * Built-in widgets implement standard editing operations like cursor movement,
     * text deletion, history navigation, etc.
     *
     * @return a map of built-in widget names to widgets
     */
    Map<String, Widget> getBuiltinWidgets();

    /**
     * Returns the current line buffer.
     * <p>
     * The buffer contains the text that the user is currently editing.
     * It provides methods for manipulating the text and cursor position.
     *
     * @return the current line buffer
     */
    Buffer getBuffer();

    /**
     * Returns the application name associated with this LineReader.
     * <p>
     * The application name is used for various purposes, such as naming
     * history files and identifying the application in terminal titles.
     *
     * @return the application name
     */
    String getAppName();

    /**
     * Push back a key sequence that will be later consumed by the line reader.
     * This method can be used after reading the cursor position using
     * {@link Terminal#getCursorPosition(IntConsumer)}.
     *
     * @param macro the key sequence to push back
     * @see Terminal#getCursorPosition(IntConsumer)
     * @see #readMouseEvent()
     */
    void runMacro(String macro);

    /**
     * Read a mouse event when the {@link org.jline.utils.InfoCmp.Capability#key_mouse} sequence
     * has just been read on the input stream.
     * Compared to {@link Terminal#readMouseEvent()}, this method takes into account keys
     * that have been pushed back using {@link #runMacro(String)}.
     *
     * @return the mouse event
     * @see #runMacro(String)
     * @see Terminal#getCursorPosition(IntConsumer)
     */
    MouseEvent readMouseEvent();

    /**
     * Returns the history associated with this LineReader.
     * <p>
     * The history stores previously entered command lines and provides
     * methods for navigating, searching, and managing history entries.
     *
     * @return the command history
     */
    History getHistory();

    /**
     * Returns the parser associated with this LineReader.
     * <p>
     * The parser is responsible for breaking command lines into tokens
     * according to the syntax rules of the shell or application.
     *
     * @return the parser
     */
    Parser getParser();

    /**
     * Returns the highlighter associated with this LineReader.
     * <p>
     * The highlighter is responsible for applying syntax highlighting
     * to the command line as the user types.
     *
     * @return the highlighter
     */
    Highlighter getHighlighter();

    /**
     * Returns the expander associated with this LineReader.
     * <p>
     * The expander is responsible for expanding special syntax in the command line,
     * such as history references (e.g., !!, !$) and variables (e.g., $HOME).
     *
     * @return the expander
     */
    Expander getExpander();

    /**
     * Returns all key maps registered with this LineReader.
     * <p>
     * Key maps define the mappings from key sequences to actions for different
     * editing modes (e.g., Emacs mode, Vi command mode, Vi insert mode).
     *
     * @return a map of key map names to key maps
     */
    Map<String, KeyMap<Binding>> getKeyMaps();

    /**
     * Returns the name of the currently active key map.
     * <p>
     * The active key map determines how key presses are interpreted and
     * which actions they trigger.
     *
     * @return the name of the active key map
     */
    String getKeyMap();

    /**
     * Sets the active key map by name.
     * <p>
     * The active key map determines how key presses are interpreted and
     * which actions they trigger.
     *
     * @param name the name of the key map to activate
     * @return true if the key map was successfully set, false if the key map does not exist
     */
    boolean setKeyMap(String name);

    /**
     * Returns the currently active key map.
     * <p>
     * The active key map determines how key presses are interpreted and
     * which actions they trigger.
     *
     * @return the active key map
     */
    KeyMap<Binding> getKeys();

    /**
     * Returns the parsed representation of the current line.
     * <p>
     * The parsed line contains the tokenized form of the current input line,
     * broken down according to the syntax rules of the parser.
     *
     * @return the parsed line, or null if the line has not been parsed yet
     */
    ParsedLine getParsedLine();

    /**
     * Returns the current search term when in history search mode.
     * <p>
     * This is the string that the user is searching for in the command history.
     *
     * @return the current search term, or null if not in search mode
     */
    String getSearchTerm();

    /**
     * Returns the type of the currently active region selection.
     * <p>
     * The region is a selected portion of text in the buffer, similar to
     * a selection in a text editor.
     *
     * @return the type of the active region, or {@link RegionType#NONE} if no region is active
     */
    RegionType getRegionActive();

    /**
     * Returns the mark position of the currently active region.
     * <p>
     * The mark is one endpoint of the selected region, with the cursor
     * being the other endpoint.
     *
     * @return the position of the mark, or -1 if no region is active
     */
    int getRegionMark();

    /**
     * Adds a collection of commands to the input buffer for execution.
     * <p>
     * These commands will be executed one by one when the user accepts the current line.
     * This is useful for implementing features like command scripts or macros.
     *
     * @param commands the commands to add to the buffer
     */
    void addCommandsInBuffer(Collection<String> commands);

    /**
     * Opens a file in an external editor and adds its contents to the input buffer.
     * <p>
     * This method allows the user to edit a file in their preferred text editor
     * and then have its contents added to the input buffer for execution.
     *
     * @param file the file to edit, or null to create a temporary file
     * @throws Exception if an error occurs while editing the file
     * @see #editAndAddInBuffer(Path)
     */
    default void editAndAddInBuffer(File file) throws Exception {
        editAndAddInBuffer(file != null ? file.toPath() : null);
    }

    /**
     * Opens a file in an external editor and adds its contents to the input buffer.
     * <p>
     * This method allows the user to edit a file in their preferred text editor
     * and then have its contents added to the input buffer for execution.
     *
     * @param file the file to edit, or null to create a temporary file
     * @throws Exception if an error occurs while editing the file
     */
    void editAndAddInBuffer(Path file) throws Exception;

    /**
     * Returns the last key binding that was processed.
     * <p>
     * This is the string representation of the last key sequence that
     * triggered an action.
     *
     * @return the last key binding, or null if no binding has been processed
     */
    String getLastBinding();

    /**
     * Returns the current tail tip text.
     * <p>
     * The tail tip is a hint or suggestion displayed at the end of the current line,
     * typically showing command syntax or parameter information.
     *
     * @return the current tail tip text, or null if no tail tip is set
     */
    String getTailTip();

    /**
     * Sets the tail tip text.
     * <p>
     * The tail tip is a hint or suggestion displayed at the end of the current line,
     * typically showing command syntax or parameter information.
     *
     * @param tailTip the tail tip text to display, or null to clear the tail tip
     */
    void setTailTip(String tailTip);

    /**
     * Sets the type of auto-suggestion to use.
     * <p>
     * Auto-suggestions provide inline completion suggestions as the user types,
     * based on history, completers, or other sources.
     *
     * @param type the type of auto-suggestion to use
     */
    void setAutosuggestion(SuggestionType type);

    /**
     * Returns the current auto-suggestion type.
     * <p>
     * Auto-suggestions provide inline completion suggestions as the user types,
     * based on history, completers, or other sources.
     *
     * @return the current auto-suggestion type
     */
    SuggestionType getAutosuggestion();

    /**
     * Clears any internal buffers and sensitive data.
     * <p>
     * This method is used to ensure that sensitive information, such as passwords
     * or other confidential data, is removed from memory when it's no longer needed.
     * It should be called when the LineReader is no longer in use or before reading
     * sensitive information.
     */
    void zeroOut();
}
