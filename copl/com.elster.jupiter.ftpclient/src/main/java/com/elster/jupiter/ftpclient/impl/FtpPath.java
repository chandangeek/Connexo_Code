package com.elster.jupiter.ftpclient.impl;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.elster.jupiter.util.streams.Predicates.not;

final class FtpPath implements Path {

    private static final Pattern SEPARATOR = Pattern.compile("/");
    private final FtpFileSystem fileSystem;
    private final List<String> names;

    FtpPath(FtpFileSystem fileSystem, String name) {
        this.fileSystem = fileSystem;
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        boolean hasRootPart = name.startsWith("/");
        String namePart = hasRootPart ? name.substring(1) : name;
        if (hasRootPart) {
            builder.add("/");
        }
        Pattern.compile("/").splitAsStream(namePart)
                .forEach(builder::add);
        names = builder.build();
    }

    FtpPath(FtpFileSystem fileSystem, List<String> names) {
        this.fileSystem = fileSystem;
        if (names.isEmpty()) {
            this.names = Collections.emptyList();
            return;
        }
        ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
        if (names.get(0).startsWith("/")) {
            builder.add("/");
        }
        names.stream()
                .flatMap(SEPARATOR::splitAsStream)
                .filter(not(String::isEmpty))
                .forEach(builder::add);
        this.names = builder.build();
    }

    @Override
    public FtpFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return !names.isEmpty() && "/".equals(names.get(0));
    }

    @Override
    public Path getRoot() {
        return isAbsolute() ? new FtpPath(fileSystem, "/") : null;
    }

    @Override
    public Path getFileName() {
        if (isRoot()) {
            return null;
        }
        if (getParent() == null) {
            return this;
        }
        return new FtpPath(fileSystem, names.get(names.size() - 1));
    }

    @Override
    public Path getParent() {
        if (names.size() <= 1) {
            return null;
        }
        return new FtpPath(fileSystem, names.subList(0, names.size() - 1));
    }

    @Override
    public int getNameCount() {
        return names.size() - (isAbsolute() ? 1 : 0);
    }

    @Override
    public Path getName(int index) {
        return new FtpPath(fileSystem, Collections.singletonList(namesWithoutRoot().get(index)));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return new FtpPath(fileSystem, names.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        if (!(other instanceof FtpPath)) {
            return false;
        }
        FtpPath otherFtpPath = (FtpPath) other;
        List<String> ancestors = otherFtpPath.names;
        return names.size() >= ancestors.size() && names.subList(0, ancestors.size()).equals(ancestors);
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(new FtpPath(fileSystem, other));
    }

    @Override
    public boolean endsWith(Path other) {
        if (!(other instanceof FtpPath)) {
            return false;
        }
        FtpPath otherFtpPath = (FtpPath) other;
        List<String> ancestors = otherFtpPath.names;
        List<String> myAncestors = names;
        return myAncestors.size() >= ancestors.size() && myAncestors.subList(myAncestors.size() - ancestors.size(), myAncestors.size()).equals(ancestors);
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(new FtpPath(fileSystem, other));
    }

    @Override
    public Path normalize() {
        List<String> normalizedNames = new LinkedList<>(this.names);
        removeSameDirectories(normalizedNames);
        removeNavigateParentDirectories(normalizedNames);
        return new FtpPath(fileSystem, normalizedNames);
    }

    private void removeNavigateParentDirectories(List<String> normalizedNames) {
        int size = normalizedNames.size();
        IntStream.range(0, size)
                .map(i -> size - i - 1)
                .forEach(i -> {
                    if (i + 1 < normalizedNames.size() && "..".equals(normalizedNames.get(i + 1)) && !"..".equals(normalizedNames.get(i))) {
                        normalizedNames.remove(i + 1);
                        normalizedNames.remove(i);
                    }
                });
    }

    private void removeSameDirectories(List<String> normalizedNames) {
        int size = normalizedNames.size();
        IntStream.range(0, size)
                .map(i -> size - i - 1)
                .forEach(i -> {
                    if (".".equals(normalizedNames.get(i))) {
                        normalizedNames.remove(i);
                    }
                });
    }

    @Override
    public Path resolve(Path other) {
        if (!(other instanceof FtpPath)) {
            throw new IllegalArgumentException();
        }
        if (other.isAbsolute()) {
            return other;
        }
        ArrayList<String> resolved = new ArrayList<>(names);
        resolved.addAll(((FtpPath) other).names);
        return new FtpPath(fileSystem, resolved);
    }

    @Override
    public Path resolve(String other) {
        return resolve(new FtpPath(fileSystem, other));
    }

    @Override
    public Path resolveSibling(Path other) {
        if (other.isAbsolute()) {
            return other;
        }
        if (isRoot() || getParent() == null) {
            return other;
        }
        return getParent().resolve(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(new FtpPath(fileSystem, other));
    }

    @Override
    public Path relativize(Path other) {
        if (!(other instanceof FtpPath)) {
            throw new IllegalArgumentException();
        }
        FtpPath otherFtpPath = (FtpPath) other; // TODO

        return null;
    }

    @Override
    public URI toUri() {
        try {
            return new URI(EdtFtpjFileSystemProvider.SCHEME, fileSystem.getHost(), toAbsolutePath().toString(), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        return new FtpPath(fileSystem, "/").resolve(this);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        return namesWithoutRoot().stream()
                .map(name -> new FtpPath(fileSystem, Collections.singletonList(name)))
                .collect(Collectors.<Path>toList())
                .iterator();
    }

    private List<String> namesWithoutRoot() {
        return isAbsolute() ? names.subList(1, names.size()) : names;
    }

    @Override
    public int compareTo(Path other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtpPath paths = (FtpPath) o;
        return Objects.equals(fileSystem, paths.fileSystem) &&
                Objects.equals(names, paths.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, names);
    }

    private boolean isRoot() {
        return names.size() == 1 && "/".equals(names.get(0));
    }

    @Override
    public String toString() {
        return namesWithoutRoot().stream()
                .collect(Collectors.joining("/", isAbsolute() ? "/" : "", ""));
    }

    void createDirectory() throws IOException {
        fileSystem.createDirectory(this);
    }

    void delete() throws IOException {
        fileSystem.delete(this);
    }

    boolean isSameFile(Path path2) {
        if (path2 instanceof FtpPath) {
            FtpPath other = (FtpPath) path2;
            return toAbsolutePath().equals(other.toAbsolutePath());
        }
        return false;
    }

    boolean exists() throws IOException {
        return fileSystem.exists(this);
    }

    long size() throws IOException {
        return fileSystem.size(this);
    }
}
