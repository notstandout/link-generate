# Public File Share (Spring Boot)

A minimal production-clean web app for public file sharing.

## Architecture (brief)

- **Frontend**: single server-rendered page (`Thymeleaf`) with lightweight vanilla JS for drag/drop upload, link copy, and result rendering.
- **Backend**: Spring Boot REST endpoints for upload, public file serving, and QR generation.
- **Persistence**: `StoredFile` metadata in H2 (file mode by default), including id, original file name, storage name, type, size, timestamp.
- **Storage**: files saved to local `uploads/` directory.
- **Security choice for HTML files**: uploaded HTML is served as **attachment download**, not inline rendering, to avoid executing untrusted markup.

## Features

- Upload public files with no authentication.
- Generates random non-guessable public link: `/f/{id}`.
- Returns QR code for the public link.
- Copy link button.
- Preview for image/video/audio/pdf/text when browser supports it.
- Validation:
  - Max size: **50MB**
  - Supported MIME types:
    - images (`jpeg/png/gif/webp`)
    - videos (`mp4/webm/ogg`)
    - audio (`mpeg/ogg/wav/webm`)
    - `application/pdf`
    - `text/plain`
    - zip (`application/zip`, `application/x-zip-compressed`)
    - `text/html` (download only)
- MIME fallback by extension for clients that upload as `application/octet-stream`.

## Requirements

- Java 17+
- Maven 3.9+

## Run locally

```bash
mvn spring-boot:run
```

Open: `http://localhost:8080`

## Run tests

```bash
mvn test
```

## API overview

### `POST /api/upload`
Multipart field: `file`

Response example:

```json
{
  "id": "Q2lYfQj5mXwB8PzD9S8l4A",
  "fileName": "photo.png",
  "contentType": "image/png",
  "size": 124331,
  "publicUrl": "http://localhost:8080/f/Q2lYfQj5mXwB8PzD9S8l4A",
  "qrCodeDataUrl": "data:image/png;base64,...",
  "previewType": "image"
}
```

### `GET /f/{id}`
Public file access by id.

### `GET /api/files/{id}/qr`
Returns QR PNG for existing file id.

## Notes

- Database file: `./data/fileshare.mv.db`
- Upload folder: `./uploads`
- If you change storage path, set `app.upload-dir` in `application.properties`.
- `spring.jpa.open-in-view=false` is set to avoid lazy-load queries during view rendering.
- Maven enforcer blocks accidental `-SNAPSHOT` dependencies/plugins for more reproducible builds.
