package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.util.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class LocalFileWriter {

    private final IDataExportService dataExportService;

    LocalFileWriter(IDataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    Map<StructureMarker, Path> writeToTempFiles(List<FormattedExportData> data) {
        return decorate(data.stream())
                .partitionWhen((a, b) -> a.getStructureMarker().differsAt(b.getStructureMarker()) == 0)
                .map(this::writeToTempFile)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    private Pair<StructureMarker, Path> writeToTempFile(List<FormattedExportData> exportDatas) {
        Path tempDirectory = dataExportService.getTempDirectory();

        StructureMarker structureMarker = exportDatas.get(exportDatas.size() - 1).getStructureMarker();

        try {
            Path tempFile = createTempFile(tempDirectory, structureMarker);
            try (BufferedWriter writer = openWriter(tempFile)) {
                exportDatas.stream()
                        .map(FormattedExportData::getAppendablePayload)
                        .forEach(payload -> {
                            try {
                                writer.write(payload);
                            } catch (IOException e) {
                                throw new FileIOException(dataExportService.getThesaurus(), tempFile, e);
                            }
                        });
            }
            return Pair.of(structureMarker, tempFile);
        } catch (IOException e) {
            throw new FileIOException(dataExportService.getThesaurus(), tempDirectory, e);
        }
    }

    private Path createTempFile(Path tempDirectory, StructureMarker structureMarker) throws IOException {
        return Files.createTempFile(tempDirectory, structureMarker.getStructurePath().get(0), "tmp");
    }

    private BufferedWriter openWriter(Path tempFile) throws IOException {
        OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.TRUNCATE_EXISTING);
        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

}
