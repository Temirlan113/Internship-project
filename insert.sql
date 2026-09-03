INSERT INTO keycloak_role(id, name, realm_id, description)
VALUES ('role-admin-id-1001', 'ROLE_ADMIN', 'b401fa39-a47f-47be-b8e2-52cce2171530', 'Роль админа'),
       ('role-teacher-id-1002', 'ROLE_TEACHER', 'b401fa39-a47f-47be-b8e2-52cce2171530', 'Роль преподавателя'),
       ('role-user-id-1003', 'ROLE_USER', 'b401fa39-a47f-47be-b8e2-52cce2171530', 'Роль пользователя')

ON CONFLICT (id) DO NOTHING;

INSERT INTO user_entity (id,
                         username,
                         first_name,
                         last_name,
                         email,
                         email_constraint,
                         email_verified,
                         enabled,
                         realm_id,
                         created_timestamp)
VALUES ('b401fa39-a47f-47be-b8e2-52cce2171530', 'admin','AdminBek', 'Adminov', 'admin@gmail.com', 'admin@gmail.com', true, true,
        'b401fa39-a47f-47be-b8e2-52cce2171530', 1700000000000)
ON CONFLICT(id) DO NOTHING;

INSERT INTO credential (id,
                        type,
                        user_id,
                        secret_data,
                        credential_data,
                        created_date)
VALUES ('7396508b-4118-4af0-90a5-270c4baab513',
        'password',
        'b401fa39-a47f-47be-b8e2-52cce2171530',
        '{"value":"XJLzOLgto4UsZ+nKFnBIP/7PPUVr1YEcKXrW3E02Mug=","salt":"iExAT/GifPehwHnp2Rya6w==","additionalParameters":{}}',
        '{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}',
        1700000000000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role_mapping (role_id, user_id)
VALUES ('role-admin-id-1001', '5bae9333-417d-42ce-811f-4b4423cea94a')
ON CONFLICT (role_id, user_id) DO NOTHING;
