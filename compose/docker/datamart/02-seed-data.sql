-- 1. Генерация тестовых данных (500 сотрудников, дети, документы)

SET search_path TO datamart;
SET search_path TO datamart;

DO
$$
    DECLARE
        emp_id INTEGER;
        child_id
               INTEGER;
        i
               INTEGER;
        child_count
               INTEGER;
        doc_count
               INTEGER;
        departments
               TEXT[] := ARRAY ['IT', 'HR', 'Финансы', 'Маркетинг', 'Продажи', 'Логистика', 'Производство', 'Юридический', 'Администрация'];
    BEGIN
        -- Генерация 500 сотрудников
        FOR i IN 1..500
            LOOP
                INSERT INTO employee (name, email, department, created_at)
                VALUES ('Сотрудник ' || i,
                        'employee' || i || '@company.com',
                        departments[(i % array_length(departments, 1)) + 1],
                        CURRENT_TIMESTAMP - (floor(random() * 365 * 3) || ' days')::INTERVAL)
                RETURNING id INTO emp_id;

                -- Генерация 1-2 детей на сотрудника
                child_count
                    := floor(random() * 2) + 1;
                FOR j IN 1..child_count
                    LOOP
                        INSERT INTO children (employee_id, name, age, birth_date)
                        VALUES (emp_id,
                                'Ребенок ' || emp_id || '-' || j,
                                floor(random() * 18) + 1,
                                CURRENT_DATE - ((floor(random() * 18) + 1) * 365 || ' days')::INTERVAL)
                        RETURNING id INTO child_id;

                        -- Документы для ребенка (1-2 документа)
                        doc_count
                            := floor(random() * 2) + 1;
                        FOR k IN 1..doc_count
                            LOOP
                                INSERT INTO documents (employee_id, child_id, document_type, document_number,
                                                       issue_date, expiration_date, file_path)
                                VALUES (emp_id,
                                        child_id,
                                        CASE
                                            WHEN random() < 0.8 THEN 'Свидетельство о рождении'
                                            ELSE 'Медицинский полис' END,
                                        'CHILD-' || child_id || '-DOC-' || k,
                                        CURRENT_DATE - (floor(random() * 365 * 5) || ' days')::INTERVAL,
                                        CASE
                                            WHEN random() < 0.3 THEN NULL
                                            ELSE CURRENT_DATE + (floor(random() * 365 * 10) || ' days')::INTERVAL
                                            END,
                                        '/docs/child_' || child_id || '/doc_' || k || '.pdf');
                            END LOOP;
                    END LOOP;

                -- Документы для сотрудника (2-4 документа)
                doc_count
                    := floor(random() * 3) + 2;
                FOR k IN 1..doc_count
                    LOOP
                        INSERT INTO documents (employee_id, child_id, document_type, document_number,
                                               issue_date, expiration_date, file_path)
                        VALUES (emp_id,
                                NULL,
                                CASE (floor(random() * 5))
                                    WHEN 0 THEN 'Паспорт'
                                    WHEN 1 THEN 'Водительские права'
                                    WHEN 2 THEN 'ИНН'
                                    WHEN 3 THEN 'СНИЛС'
                                    ELSE 'Медицинский полис'
                                    END,
                                'EMP-' || emp_id || '-DOC-' || k,
                                CURRENT_DATE - (floor(random() * 365 * 10) || ' days')::INTERVAL,
                                CASE
                                    WHEN random() < 0.5 THEN
                                        CURRENT_DATE + (floor(random() * 365 * 10) || ' days')::INTERVAL
                                    ELSE NULL END,
                                '/docs/emp_' || emp_id || '/doc_' || k || '.pdf');
                    END LOOP;
            END LOOP;

        RAISE
            NOTICE 'Инициализация базы данных завершена:';
        RAISE
            NOTICE '- Сотрудников: %', (SELECT COUNT(*) FROM employee);
        RAISE
            NOTICE '- Детей: %', (SELECT COUNT(*) FROM children);
        RAISE
            NOTICE '- Документов: %', (SELECT COUNT(*) FROM documents);
    END
$$;