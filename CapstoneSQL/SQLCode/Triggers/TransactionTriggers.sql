/* Updates the riders balance once a transaction has been made */
create trigger riderBalanceUpdate_trigger
on Rideshare.RideTransaction
after UPDATE, INSERT
as
begin
DECLARE @USRID as int = (
	select rr.riderID from inserted ins
	join RideShare.RideReceipt rr on rr.rideID = ins.rideID
)
update Rideshare.Usr 

set balance = (
	select (made.totalMade - owed.totalOwed)
	from (
		select ISNULL(sum(rt.rideTotal), 0) as totalMade
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		join rideshare.Route r on r.routeID = rr.routeID
		where r.driverID = @USRID
	) made,
	(
		select ISNULL(sum(rt.rideTotal), 0) as totalOwed
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		where rr.riderID = @USRID
	) owed
)
from inserted
where Rideshare.Usr.UsrID = @USRID
end

/* Updates the riders balance once a transaction has been removed */
go
create trigger riderBalanceDelete_trigger
on Rideshare.RideTransaction
after delete
as
begin
DECLARE @USRID as int = (
	select rr.riderID from deleted del
	join RideShare.RideReceipt rr on rr.rideID = del.rideID
)
update Rideshare.Usr 

set balance = (
	select (made.totalMade - owed.totalOwed)
	from (
		select ISNULL(sum(rt.rideTotal), 0) as totalMade
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		join rideshare.Route r on r.routeID = rr.routeID
		where r.driverID = @USRID
	) made,
	(
		select ISNULL(sum(rt.rideTotal), 0) as totalOwed
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		where rr.riderID = @USRID
	) owed
)
from deleted
where Rideshare.Usr.UsrID = @USRID
end

/* Updates the driver balance once a transaction has been made */
go
create trigger driverBalanceUpdate_trigger
on Rideshare.RideTransaction
after UPDATE, INSERT
as
begin
DECLARE @USRID as int = (
	select r.driverID from inserted ins
	join RideShare.RideReceipt rr on rr.rideID = ins.rideID
	join RideShare.Route r on r.routeID = rr.routeID
)
update Rideshare.Usr 

set balance = (
	select (made.totalMade - owed.totalOwed)
	from (
		select ISNULL(sum(rt.rideTotal), 0) as totalMade
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		join rideshare.Route r on r.routeID = rr.routeID
		where r.driverID = @USRID
	) made,
	(
		select ISNULL(sum(rt.rideTotal), 0) as totalOwed
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		where rr.riderID = @USRID
	) owed
)
from inserted
where Rideshare.Usr.UsrID = @USRID
end

/* Updates the drivers balance once a transaction has been removed */
go
create trigger driverBalanceDelete_trigger
on Rideshare.RideTransaction
after delete
as
begin
DECLARE @USRID as int = (
	select r.driverID from deleted ins
	join RideShare.RideReceipt rr on rr.rideID = ins.rideID
	join RideShare.Route r on r.routeID = rr.routeID
)
update Rideshare.Usr 

set balance = (
	select (made.totalMade - owed.totalOwed)
	from (
		select ISNULL(sum(rt.rideTotal), 0) as totalMade
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		join rideshare.Route r on r.routeID = rr.routeID
		where r.driverID = @USRID
	) made,
	(
		select ISNULL(sum(rt.rideTotal), 0) as totalOwed
		from rideshare.RideTransaction rt
		join rideshare.RideReceipt rr on rr.rideID = rt.rideID
		where rr.riderID = @USRID
	) owed
)
from deleted
where Rideshare.Usr.UsrID = @USRID
end