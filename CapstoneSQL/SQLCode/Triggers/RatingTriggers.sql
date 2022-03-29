/*Updates the riders score on new or edited review*/
create trigger riderScoreUpdate_trigger
on Rideshare.Review
after UPDATE, INSERT
as
begin
update Rideshare.Usr 
set riderScore = (
	SELECT AVG(rating)
	FROM Rideshare.Review INNER JOIN
	Rideshare.Usr ON Rideshare.Review.recipientID = Rideshare.Usr.UsrID 
	where rideshare.Usr.UsrID = inserted.recipientID and rideshare.Review.type ='r'
)
from inserted
where Rideshare.Usr.UsrID = inserted.recipientID
end
go

/*Updates the drivers score on new or edited review*/
create trigger driverScoreUpdate_trigger
on Rideshare.Review
after UPDATE, INSERT
as
begin
update Rideshare.Usr 
set driverScore = (
	SELECT AVG(rating)
	FROM Rideshare.Review INNER JOIN
	Rideshare.Usr ON Rideshare.Review.recipientID = Rideshare.Usr.UsrID 
	where rideshare.Usr.UsrID = inserted.recipientID and rideshare.Review.type ='d'
)
from inserted
where Rideshare.Usr.UsrID = inserted.recipientID
end
go

/*Updates the riders score on deleted review*/
create trigger riderScoreDelete_trigger
on Rideshare.Review
after delete
as
begin
update Rideshare.Usr 
set riderScore = (
	SELECT AVG(rating)
	FROM Rideshare.Review INNER JOIN
	Rideshare.Usr ON Rideshare.Review.recipientID = Rideshare.Usr.UsrID 
	where rideshare.Usr.UsrID = deleted.recipientID and rideshare.Review.type ='r'
)
from deleted
where Rideshare.Usr.UsrID = deleted.recipientID
end
go

/*Updates the drivers score on deleted review*/
create trigger driverScoreDelete_trigger
on Rideshare.Review
after delete
as
begin
update Rideshare.Usr 
set driverScore = (
	SELECT AVG(rating)
	FROM Rideshare.Review INNER JOIN
	Rideshare.Usr ON Rideshare.Review.recipientID = Rideshare.Usr.UsrID 
	where rideshare.Usr.UsrID = deleted.recipientID and rideshare.Review.type ='d'
)
from deleted
where Rideshare.Usr.UsrID = deleted.recipientID
end
go