CREATE SCHEMA IF NOT EXISTS datamart;
COMMENT ON SCHEMA datamart IS 'Схема для хранения данных о сотрудниках и их семьях';

SET search_path TO datamart;

-- Таблица employee
CREATE TABLE IF NOT EXISTS employee
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN employee.id IS 'Уникальный ID сотрудника';
COMMENT ON COLUMN employee.name IS 'ФИО сотрудника';
COMMENT ON COLUMN employee.email IS 'Email сотрудника (уникальный)';
COMMENT ON COLUMN employee.department IS 'Отдел сотрудника';
COMMENT ON COLUMN employee.created_at IS 'Дата создания записи';

-- Таблица children
CREATE TABLE IF NOT EXISTS children
(
    id          SERIAL PRIMARY KEY,
    employee_id INTEGER      NOT NULL REFERENCES employee (id),
    name        VARCHAR(100) NOT NULL,
    age         INTEGER,
    birth_date  DATE
);

COMMENT ON COLUMN children.id IS 'Уникальный ID ребенка';
COMMENT ON COLUMN children.employee_id IS 'ID родителя-сотрудника';
COMMENT ON COLUMN children.name IS 'ФИО ребенка';
COMMENT ON COLUMN children.age IS 'Возраст ребенка';
COMMENT ON COLUMN children.birth_date IS 'Дата рождения ребенка';

-- Таблица documents
CREATE TABLE IF NOT EXISTS documents
(
    id              SERIAL PRIMARY KEY,
    employee_id     INTEGER             NOT NULL REFERENCES employee (id),
    child_id        INTEGER REFERENCES children (id),
    document_type   VARCHAR(50)         NOT NULL,
    document_number VARCHAR(100) UNIQUE NOT NULL,
    issue_date      DATE,
    expiration_date DATE,
    file_path       TEXT
);

COMMENT ON COLUMN documents.id IS 'Уникальный ID документа';
COMMENT ON COLUMN documents.employee_id IS 'ID сотрудника-владельца';
COMMENT ON COLUMN documents.child_id IS 'ID ребенка (если документ детский)';
COMMENT ON COLUMN documents.document_type IS 'Тип документа';
COMMENT ON COLUMN documents.document_number IS 'Номер документа (уникальный)';
COMMENT ON COLUMN documents.issue_date IS 'Дата выдачи';
COMMENT ON COLUMN documents.expiration_date IS 'Срок действия';
COMMENT ON COLUMN documents.file_path IS 'Путь к файлу документа';