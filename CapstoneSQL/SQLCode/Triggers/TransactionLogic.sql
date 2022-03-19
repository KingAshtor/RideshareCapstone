/* calculates money a user owes*/
select sum(rt.rideTotal) as totalOwed
from rideshare.RideTransaction rt
join rideshare.RideReceipt rr on rr.rideID = rt.rideID
where rr.riderID = 8

/* calculates money a user has made */
select sum(rt.rideTotal) as totalMade
from rideshare.RideTransaction rt
join rideshare.RideReceipt rr on rr.rideID = rt.rideID
join rideshare.Route r on r.routeID = rr.routeID
where r.driverID = 7

/* gets the balance by subracting amount owed from amount made */
select (made.totalMade - owed.totalOwed)
from (
	select ISNULL(sum(rt.rideTotal), 0) as totalMade
	from rideshare.RideTransaction rt
	join rideshare.RideReceipt rr on rr.rideID = rt.rideID
	join rideshare.Route r on r.routeID = rr.routeID
	where r.driverID = 8
) made,
(
	select ISNULL(sum(rt.rideTotal), 0) as totalOwed
	from rideshare.RideTransaction rt
	join rideshare.RideReceipt rr on rr.rideID = rt.rideID
	where rr.riderID = 8
) owed
go