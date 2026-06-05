package com.example.novel2script.service;

import com.example.novel2script.dto.ChapterDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NovelParserService {

    private static final Pattern[] CHAPTER_PATTERNS = {
        Pattern.compile("^(第[一二三四五六七八九十百千\\d]+章)\\s*(.*)$", Pattern.MULTILINE),
        Pattern.compile("^(Chapter\\s+\\d+)\\s*(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
        Pattern.compile("^(CHAPTER\\s+\\d+)\\s*(.*)$", Pattern.MULTILINE),
        Pattern.compile("^(\\d+)\\.[\\s-]*(.*)$", Pattern.MULTILINE),
        Pattern.compile("^(第\\d+章)\\s*(.*)$", Pattern.MULTILINE)
    };

    public List<ChapterDto> parseChapters(String text) {
        List<ChapterDto> chapters = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chapters;
        }

        String[] lines = text.split("\\n");
        ChapterDto currentChapter = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            ChapterDto matched = matchChapterHeader(line);
            if (matched != null) {
                if (currentChapter != null) {
                    currentChapter.setContentPreview(currentContent.toString().trim());
                    chapters.add(currentChapter);
                    currentContent = new StringBuilder();
                }
                currentChapter = matched;
            } else if (currentChapter != null) {
                currentContent.append(line).append("\n");
            }
        }

        if (currentChapter != null) {
            currentChapter.setContentPreview(currentContent.toString().trim());
            chapters.add(currentChapter);
        }

        return chapters;
    }

    private ChapterDto matchChapterHeader(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return null;
        }

        for (Pattern pattern : CHAPTER_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                ChapterDto dto = new ChapterDto();
                String title = matcher.group(1);
                dto.setTitle(title);
                if (matcher.groupCount() > 1 && matcher.group(2) != null) {
                    String subtitle = matcher.group(2).trim();
                    if (!subtitle.isEmpty()) {
                        dto.setTitle(title + " " + subtitle);
                    }
                }
                return dto;
            }
        }
        return null;
    }

    public int getChapterCount(String text) {
        return parseChapters(text).size();
    }

    public String extractTitle(String text) {
        List<ChapterDto> chapters = parseChapters(text);
        if (!chapters.isEmpty()) {
            return chapters.get(0).getTitle();
        }
        return "未命名剧本";
    }
}
