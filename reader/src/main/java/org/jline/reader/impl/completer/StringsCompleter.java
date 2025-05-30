/*
 * Copyright (c) 2002-2025, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.reader.impl.completer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;

/**
 * Completer for a set of strings.
 *
 * @since 2.3
 */
public class StringsCompleter implements Completer {
    protected Collection<Candidate> candidates;
    protected Supplier<Collection<String>> stringsSupplier;

    public StringsCompleter() {
        this(Collections.<Candidate>emptyList());
    }

    public StringsCompleter(Supplier<Collection<String>> stringsSupplier) {
        assert stringsSupplier != null;
        candidates = null;
        this.stringsSupplier = stringsSupplier;
    }

    public StringsCompleter(String... strings) {
        this(Arrays.asList(strings));
    }

    public StringsCompleter(Iterable<String> strings) {
        assert strings != null;
        this.candidates = new ArrayList<>();
        for (String string : strings) {
            candidates.add(new Candidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
        }
    }

    public StringsCompleter(Candidate... candidates) {
        this(Arrays.asList(candidates));
    }

    public StringsCompleter(Collection<Candidate> candidates) {
        assert candidates != null;
        this.candidates = new ArrayList<>(candidates);
    }

    @Override
    public void complete(LineReader reader, final ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;
        if (this.candidates != null) {
            candidates.addAll(this.candidates);
        } else {
            for (String string : stringsSupplier.get()) {
                candidates.add(new Candidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
            }
        }
    }

    @Override
    public String toString() {
        String value = candidates != null ? candidates.toString() : "{" + stringsSupplier.toString() + "}";
        return "StringsCompleter" + value;
    }
}
