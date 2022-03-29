CREATE TABLE Rideshare.Address (
  addressID int identity,
  line1 varchar(32),
  line2 varchar(32),
  city varchar(32),
  state char(2),
  zip char(5),
  PRIMARY KEY (addressID)
);

CREATE TABLE Rideshare.Usr (
  usrID int identity,
  email varchar(320),
  fName varchar(16),
  lName varchar(16),
  hashedPwd varchar(64),
  salt varchar(36),
  riderScore int,
  driverScore int,
  balance decimal(6,2),
  homeAddress int,
  PRIMARY KEY (usrID),
  FOREIGN KEY (homeAddress) REFERENCES Rideshare.Address(addressID)
);

CREATE TABLE Rideshare.Route (
  routeID int identity,
  startAddressID int,
  endAddressID int,
  driverID int,
  gasPrice decimal(6,2),
  repeated bit,
  PRIMARY KEY (routeID),
  FOREIGN KEY (startAddressID) REFERENCES Rideshare.Address(addressID),
  FOREIGN KEY (endAddressID) REFERENCES Rideshare.Address(addressID),
  FOREIGN KEY (driverID) REFERENCES Rideshare.Usr(usrID)
);

CREATE TABLE Rideshare.RideReceipt (
  rideID int identity,
  routeID int,
  riderID int,
  rideDateTime datetime,
  rideDistance decimal(6,6),
  accepted bit,
  started bit,
  completed bit,
  PRIMARY KEY (rideID),
  FOREIGN KEY (riderID) REFERENCES Rideshare.Usr(usrID),
  FOREIGN KEY (routeID) REFERENCES Rideshare.Route(routeID)
);

CREATE TABLE Rideshare.RideTransaction (
  transactionID int identity,
  rideID int,
  transcationDate datetime,
  rideCost decimal(6,2),
  gasFee decimal(6,2),
  ptcFee decimal(6,2),
  tip decimal(6,2),
  rideTotal decimal(6,2),
  PRIMARY KEY (transactionID),
  FOREIGN KEY (rideID) REFERENCES Rideshare.RideReceipt(rideID)
);

CREATE TABLE Rideshare.Review (
  reviewID int identity,
  senderID int,
  recipientID int,
  rating int,
  description varchar(2000),
  date datetime,
  type char(1),
  PRIMARY KEY (reviewID),
  FOREIGN KEY (recipientID) REFERENCES Rideshare.Usr(usrID),
  FOREIGN KEY (senderID) REFERENCES Rideshare.Usr(usrID)
);

CREATE TABLE Rideshare.CarInfo (
  infoID int identity,
  ownerID int,
  make varchar(32),
  model varchar(64),
  year smallInt,
  color varchar(16),
  plateNumber varchar(8),
  plateState char(2),
  avgMilage decimal(6,6),
  PRIMARY KEY (infoID),
  FOREIGN KEY (ownerID) REFERENCES Rideshare.Usr(usrID)
);

CREATE TABLE Rideshare.DriverClearance (
  authID int identity,
  userID int,
  photoString char(64),
  PRIMARY KEY (authID),
  FOREIGN KEY (userID) REFERENCES Rideshare.Usr(usrID)
);

CREATE TABLE Rideshare.Role (
  roleID int identity,
  usrID int,
  value varchar(64),
  PRIMARY KEY (roleID),
  FOREIGN KEY (usrID) REFERENCES Rideshare.Usr(usrID)
);

