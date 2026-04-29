package io.camunda.dto;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.generator.java.annotation.FeelMode;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * PDF Connector request types for Operations API.
 * Each operation is a separate record - no nested structure needed.
 */
public class PdfConnectorRequest {

    // Merge PDFs operation
    public record MergePdfs(
        @TemplateProperty(
            id = "merge-documents",
            group = "operation",
            label = "PDF Documents",
            description = "List of PDF documents to merge (in order)",
            feel = FeelMode.required,
            type = TemplateProperty.PropertyType.String
        )
        @NotNull @NotEmpty
        List<Document> documents,

        @TemplateProperty(
            id = "merge-outputFilename",
            group = "operation",
            label = "Output Filename",
            description = "Name for the merged PDF file (e.g., 'merged-output.pdf')",
            defaultValue = "merged.pdf"
        )
        @NotBlank
        String outputFilename
    ) {}

    // Split by page operation
    public record SplitByPage(
        @TemplateProperty(
            id = "splitByPage-document",
            group = "operation",
            label = "PDF Document",
            description = "The PDF document to split",
            feel = FeelMode.required,
            type = TemplateProperty.PropertyType.String
        )
        @NotNull
        Document document,

        @TemplateProperty(
            id = "splitByPage-pagesPerFile",
            group = "operation",
            label = "Pages per File",
            description = "Number of pages per output file (1 = one page per file)"
        )
        @Min(1)
        Integer pagesPerFile,

        @TemplateProperty(
            id = "splitByPage-outputPattern",
            group = "operation",
            label = "Output Filename Pattern",
            description = "Pattern for output filenames. Use {index} for file number (e.g., 'page-{index}.pdf')",
            defaultValue = "split-{index}.pdf"
        )
        @NotBlank
        String outputPattern
    ) {}

    // Split by range operation
    public record SplitByRange(
        @TemplateProperty(
            id = "splitByRange-document",
            group = "operation",
            label = "PDF Document",
            description = "The PDF document to split",
            feel = FeelMode.required,
            type = TemplateProperty.PropertyType.String
        )
        @NotNull
        Document document,

        @TemplateProperty(
            id = "splitByRange-pageRanges",
            group = "operation",
            label = "Page Ranges",
            description = "Comma-separated page ranges (e.g., '1-3,5-7,10-15'). Pages are 1-indexed.",
            feel = FeelMode.optional
        )
        @NotBlank
        String pageRanges,

        @TemplateProperty(
            id = "splitByRange-outputPattern",
            group = "operation",
            label = "Output Filename Pattern",
            description = "Pattern for output filenames. Use {index} for file number",
            defaultValue = "range-{index}.pdf"
        )
        @NotBlank
        String outputPattern
    ) {}

    // Split by bookmark operation
    public record SplitByBookmark(
        @TemplateProperty(
            id = "splitByBookmark-document",
            group = "operation",
            label = "PDF Document",
            description = "The PDF document to split by bookmarks",
            feel = FeelMode.required,
            type = TemplateProperty.PropertyType.String
        )
        @NotNull
        Document document,

        @TemplateProperty(
            id = "splitByBookmark-topLevelOnly",
            group = "operation",
            label = "Top-Level Only",
            description = "Split only by top-level bookmarks (ignore nested bookmarks)",
            type = TemplateProperty.PropertyType.Boolean
        )
        Boolean topLevelOnly,

        @TemplateProperty(
            id = "splitByBookmark-outputPattern",
            group = "operation",
            label = "Output Filename Pattern",
            description = "Pattern for output filenames. Use {bookmark} for bookmark title, {index} for number",
            defaultValue = "{bookmark}.pdf"
        )
        @NotBlank
        String outputPattern
    ) {}

    // Split by size operation
    public record SplitBySize(
        @TemplateProperty(
            id = "splitBySize-document",
            group = "operation",
            label = "PDF Document",
            description = "The PDF document to split by size",
            feel = FeelMode.required,
            type = TemplateProperty.PropertyType.String
        )
        @NotNull
        Document document,

        @TemplateProperty(
            id = "splitBySize-maxFileSizeMb",
            group = "operation",
            label = "Max File Size (MB)",
            description = "Target maximum file size in megabytes. Will split when size is approached."
        )
        @Min(1) @Max(100)
        Integer maxFileSizeMb,

        @TemplateProperty(
            id = "splitBySize-outputPattern",
            group = "operation",
            label = "Output Filename Pattern",
            description = "Pattern for output filenames. Use {index} for file number",
            defaultValue = "part-{index}.pdf"
        )
        @NotBlank
        String outputPattern
    ) {}
}
