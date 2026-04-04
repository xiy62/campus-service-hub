CREATE TABLE IF NOT EXISTS service_request
(
    id            UUID PRIMARY KEY,
    request_type  VARCHAR(100) NOT NULL,
    student_id    VARCHAR(100) UNIQUE NOT NULL,
    student_email VARCHAR(255) UNIQUE NOT NULL,
    department    VARCHAR(255) NOT NULL,
    submitted_at  TIMESTAMP NOT NULL,
    status        VARCHAR(50) NOT NULL
);

INSERT INTO service_request (id, request_type, student_id, student_email, department, submitted_at, status)
SELECT '123e4567-e89b-12d3-a456-426614174000',
       'NETWORK_ACCESS',
       'S1001001',
       'alex.chen@campus.edu',
       'Computer Science',
       '2026-03-01 09:00:00',
       'PROVISIONED'
WHERE NOT EXISTS (SELECT 1 FROM service_request WHERE id = '123e4567-e89b-12d3-a456-426614174000');

INSERT INTO service_request (id, request_type, student_id, student_email, department, submitted_at, status)
SELECT '123e4567-e89b-12d3-a456-426614174001',
       'DORM_MAINTENANCE',
       'S1001002',
       'mia.wang@campus.edu',
       'Student Housing',
       '2026-03-02 13:30:00',
       'SUBMITTED'
WHERE NOT EXISTS (SELECT 1 FROM service_request WHERE id = '123e4567-e89b-12d3-a456-426614174001');

INSERT INTO service_request (id, request_type, student_id, student_email, department, submitted_at, status)
SELECT '123e4567-e89b-12d3-a456-426614174002',
       'SOFTWARE_LICENSE',
       'S1001003',
       'liam.kim@campus.edu',
       'Engineering',
       '2026-03-03 11:15:00',
       'FAILED'
WHERE NOT EXISTS (SELECT 1 FROM service_request WHERE id = '123e4567-e89b-12d3-a456-426614174002');
