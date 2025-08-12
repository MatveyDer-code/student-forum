INSERT INTO users (username, password, email, role_id)
VALUES
  ('teststudent', '$2a$12$QSV8oNUuyuqFkGdE.WryjuDOHeLQsMlwUVYebmdObBg/.eaSCzAym', 'student@test.com', (SELECT id FROM roles WHERE name='STUDENT')),
  ('testmoderator', '$2a$12$fXUh.TUo7TvTdOLQy0vi.OPYgzua5QiXUwNCpzRuyTTtBMhlzaJ9C', 'moderator@test.com', (SELECT id FROM roles WHERE name='MODERATOR')),
  ('testteacher', '$2a$12$W2XJAE/j6JR4IAp8STEpn.7yhXndP64VH.Kl3x9wiNIDakGlmraIy', 'teacher@test.com', (SELECT id FROM roles WHERE name='TEACHER'));