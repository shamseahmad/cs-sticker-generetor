# CS Sticker Name Generator

A Spring Boot web application that generates Counter-Strike sticker combinations to spell out player names, complete with real-time Steam Market pricing.

## Features

- **Name Generation**: Enter any name and get sticker combinations that spell it out
- **Steam Market Integration**: Real-time pricing from Steam Community Market
- **Price Sorting**: Sort results by price (low to high or high to low)
- **Responsive Web UI**: Modern, mobile-friendly interface
- **REST API**: Full API endpoints for integration with other applications
- **Error Handling**: Comprehensive error handling and user feedback

## Technologies Used

- **Backend**: Spring Boot 3.4.7, Java 17
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Build Tool**: Maven
- **Web Scraping**: JSoup for Steam Market data
- **JSON Processing**: Jackson

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Installation

1. **Clone the repository**:

2. **Run the application**:

3. **Access the application**:
- Web Interface: `http://localhost:5500`
- API: `http://localhost:5500/api/stickers/generate`

## API Endpoints
POST /api/stickers/generate
Content-Type: application/json

{
"name": "ALICE",
"sortOrder": "asc"
}


### Search Stickers
GET /api/stickers/search?name=BOB&sortOrder=desc


## Usage

1. **Enter a name** in the web interface
2. **Choose sorting preference** (low to high or high to low)
3. **Click "Generate Sticker Combinations"**
4. **View results** with sticker names, prices, and Steam Market links
5. **Click Steam Market links** to purchase stickers

## Configuration

The application runs on port 5500 by default. To change the port, update `application.properties`:
server.port=8080


## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── stickergenerator/
│   │               ├── controller/            # REST controllers
│   │               ├── service/               # Business logic
│   │               ├── model/                 # Data models
│   │               └── StickerGeneratorApplication.java   # Main entry point
│   └── resources/
│       ├── templates/          # Thymeleaf templates
│       ├── static/             # CSS, JS, images
│       ├── data/               # Sticker data
│       └── application.properties
```


## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.

## Acknowledgments

- Steam Community Market for pricing data
- Bootstrap for responsive UI components
- Spring Boot team for the excellent framework
