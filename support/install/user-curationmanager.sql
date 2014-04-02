CREATE ROLE curationmanager LOGIN
  ENCRYPTED PASSWORD 'md5e921032565d571caac7c037dd8bf7e06'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

CREATE DATABASE curationmanager
  WITH OWNER = curationmanager
       ENCODING = 'UTF8'
       TABLESPACE = pg_default       
       CONNECTION LIMIT = -1;
GRANT ALL ON DATABASE curationmanager TO curationmanager;
REVOKE ALL ON DATABASE curationmanager FROM public;  