## Section 1: Architecture summary
This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.

## Section 2: Numbered flow of data and control
Example format:
1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer to process the request.
4. The service layer interacts with the MySQL or MongoDB repositories to fetch or persist data.
5. The service layer returns the processed data to the controller.
6. The controller sends the data to the view (Thymeleaf template) or returns it as a JSON response for REST APIs.
7. The user receives the response and interacts with the application