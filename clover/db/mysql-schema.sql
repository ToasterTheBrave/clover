
create database clover_metadata;
use clover_metadata;

CREATE TABLE `linear_regression_algorithm` (
  `measurement_name` varchar(255) NOT NULL,
  `measurement_partition` varchar(255) NOT NULL,
  `last_processed` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`measurement_name`,`measurement_partition`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
