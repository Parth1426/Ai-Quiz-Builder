package com.quizbuilder.ai.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory knowledge base that stores curated MCQ datasets.
 * Acts as the training data for the local quiz generation model.
 * <p>
 * Loaded from JSON resource files at startup and indexed by topic / keywords
 * for fast retrieval. Supports fuzzy keyword matching, category lookups, and
 * difficulty filtering.
 */
@Slf4j
@Component
public class KnowledgeBase {

    private final List<KnowledgeEntry> entries = new ArrayList<>();
    private final Map<String, KnowledgeEntry> topicIndex = new HashMap<>();
    private final Map<String, Set<KnowledgeEntry>> keywordIndex = new HashMap<>();
    private final Map<String, List<KnowledgeEntry>> categoryIndex = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void load() {
        log.info("Loading knowledge base...");
        loadFromResource("knowledge/science.json");
        loadFromResource("knowledge/technology.json");
        loadFromResource("knowledge/history.json");
        loadFromResource("knowledge/general.json");
        loadFromResource("knowledge/lifestyle.json");
        buildIndices();
        log.info("Knowledge base loaded: {} topics across {} categories", entries.size(), categoryIndex.size());
    }

    private void loadFromResource(String resourcePath) {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            List<KnowledgeEntry> loaded = objectMapper.readValue(is, new TypeReference<List<KnowledgeEntry>>() {});
            entries.addAll(loaded);
            log.info("Loaded {} entries from {}", loaded.size(), resourcePath);
        } catch (Exception e) {
            log.warn("Could not load knowledge resource '{}': {}", resourcePath, e.getMessage());
        }
    }

    private void buildIndices() {
        for (KnowledgeEntry entry : entries) {
            String key = normalize(entry.getTopic());
            topicIndex.put(key, entry);

            categoryIndex.computeIfAbsent(
                    entry.getCategory() != null ? entry.getCategory().toLowerCase() : "general",
                    k -> new ArrayList<>()
            ).add(entry);

            // Index topic tokens
            for (String token : tokenize(entry.getTopic())) {
                keywordIndex.computeIfAbsent(token, k -> new HashSet<>()).add(entry);
            }

            // Index aliases — both as direct topic lookups and as keyword tokens.
            // This is what lets queries like "gym" find the "Fitness and Gym" entry.
            for (String alias : entry.getAliases()) {
                if (alias == null || alias.isBlank()) continue;
                String aliasKey = normalize(alias);
                topicIndex.putIfAbsent(aliasKey, entry);
                for (String token : tokenize(alias)) {
                    keywordIndex.computeIfAbsent(token, k -> new HashSet<>()).add(entry);
                }
            }
        }
    }

    /**
     * Returns an exact topic match (case-insensitive) if available.
     */
    public Optional<KnowledgeEntry> findExact(String topic) {
        if (topic == null) return Optional.empty();
        return Optional.ofNullable(topicIndex.get(normalize(topic)));
    }

    /**
     * Fuzzy match using overlapping keywords. Returns scored matches sorted desc.
     */
    public List<KnowledgeEntry> findRelated(String topic, int limit) {
        if (topic == null || topic.isBlank()) return Collections.emptyList();
        Set<String> queryTokens = new HashSet<>(tokenize(topic));
        if (queryTokens.isEmpty()) return Collections.emptyList();

        Map<KnowledgeEntry, Integer> scores = new HashMap<>();
        for (String token : queryTokens) {
            Set<KnowledgeEntry> matches = keywordIndex.getOrDefault(token, Collections.emptySet());
            for (KnowledgeEntry entry : matches) {
                scores.merge(entry, 1, Integer::sum);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<KnowledgeEntry, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<KnowledgeEntry> findByCategory(String category) {
        if (category == null) return Collections.emptyList();
        return categoryIndex.getOrDefault(category.toLowerCase(), Collections.emptyList());
    }

    public List<String> getAllTopics() {
        return entries.stream()
                .map(KnowledgeEntry::getTopic)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return categoryIndex.keySet().stream().sorted().collect(Collectors.toList());
    }

    public int size() {
        return entries.size();
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private List<String> tokenize(String s) {
        if (s == null) return Collections.emptyList();
        return Arrays.stream(s.toLowerCase().split("[^a-z0-9]+"))
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toList());
    }
}
