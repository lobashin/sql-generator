- automate add database structure and data:
```
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
	id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	content text,
	metadata json,
	embedding vector(1536)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
```
- добавить sql-скрипты для создания таблиц person, children, document:
```
-- Таблица person (люди)
CREATE TABLE IF NOT EXISTS person (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица children (дети)
CREATE TABLE IF NOT EXISTS children (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    person_id uuid NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица document (документы)
CREATE TABLE IF NOT EXISTS document (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    uploaded_by uuid REFERENCES person(id),
    child_id uuid REFERENCES children(id) ON DELETE SET NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_person_email ON person(email);
CREATE INDEX IF NOT EXISTS idx_children_person_id ON children(person_id);
CREATE INDEX IF NOT EXISTS idx_document_uploaded_by ON document(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_document_child_id ON document(child_id);
CREATE INDEX IF NOT EXISTS idx_document_uploaded_at ON document(uploaded_at);
```
