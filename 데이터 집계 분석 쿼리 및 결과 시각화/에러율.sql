SELECT
    ROUND(
            COUNT(*) FILTER (WHERE event_type = 'ERROR') * 100.0
        / COUNT(*),
            2
    ) AS error_rate_percent
FROM event_log;