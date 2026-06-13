SELECT
    event_type AS category,
    COUNT(*) AS value
FROM event_log
GROUP BY event_type;