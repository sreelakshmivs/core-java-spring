USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'poaonboarding'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`subcontractor` TO 'poaonboarding'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'poaonboarding'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'poaonboarding'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subcontractor` TO 'poaonboarding'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'poaonboarding'@'%';

FLUSH PRIVILEGES;
