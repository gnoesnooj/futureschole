SELECT
    user_id,
    COUNT(*) AS total_count
FROM
    event_log
GROUP BY
    user_id
ORDER BY
    total_count DESC;
