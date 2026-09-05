# ContactQR Web Application

A responsive, high-performance web application and digital business card platform built with modern HTML5, CSS3 Bento Grid design, and vanilla JavaScript.

## Features

- **My Card & QR Code Generator**:
  - Live dynamic QR code generation (Landing Page URL, Universal vCard 3.0, and MeCard formats).
  - High-resolution HTML5 Canvas rendering.
  - Fullscreen Digital Badge mode for networking and events.
  - Download QR code as PNG and copy payload to clipboard.
- **Interactive Public Landing Page**:
  - Bento Grid layout optimized for mobile and desktop screens.
  - Quick action buttons: Direct Call, Email, WhatsApp chat, Website, and Google Maps location.
  - Detailed contact tiles with one-click copy to clipboard.
  - Social network links: LinkedIn, WhatsApp, Telegram, GitHub, Instagram.
  - **Save to Contacts**: Triggers native `.vcf` vCard file download, which immediately opens in iOS Contacts or Android Contacts to add the card with one tap.
- **Web QR Scanner**:
  - Uses device camera via `navigator.mediaDevices.getUserMedia`.
  - Scans and decodes QR codes with native `BarcodeDetector` / canvas image processing.
  - Image file upload for scanning saved QR codes.
  - Simulation mode for instant demo testing.
- **Received Contacts Archive**:
  - Browser-persisted (`localStorage`) contact history.
  - Real-time search by name, role, company, phone, or email.
  - Export all contacts into a combined `.vcf` address book file.
- **Profile Customizer**:
  - Full editor for names, job title, company, phones, email, address, bio, social links, and auto-save preference.

## How to Run & Deploy

### Option 1: Open Locally
Simply double-click or open `index.html` in any web browser (Chrome, Safari, Firefox, Edge).

### Option 2: Local Web Server
```bash
npx serve web
# OR
python3 -m http.server 3000 --directory web
```

### Option 3: Deploy to GitHub Pages / Vercel / Netlify / Firebase
1. Upload or push the `web/` directory to your repository or host.
2. The app is completely self-contained with zero server or database dependencies.
