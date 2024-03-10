insert into addresses(id, region, city, street) values (1, 'USA', 'New York', 'John Glen Blvd, 10');
insert into addresses(id, region, city, street) values (2, 'Ukraine', 'Lvov', 'Shevchenka, 5');
insert into accommodations(id, type, address_id, size, daily_rate, availability) values (1, 'APARTMENT', 1, 'Small', 100, 2);
insert into accommodations(id, type, address_id, size, daily_rate, availability) values (2, 'HOUSE', 2, 'Medium', 150, 3);

insert into accommodations_amenities(accommodation_id, amenities) values (1, 'Bath');
insert into accommodations_amenities(accommodation_id, amenities) values (1, 'Big kitchen');
insert into accommodations_amenities(accommodation_id, amenities) values (1, 'Wi-fi');

insert into accommodations_amenities(accommodation_id, amenities) values (2, 'Bath');
insert into accommodations_amenities(accommodation_id, amenities) values (2, 'Pool');
