# Media assets guide

Use this folder structure to add screenshots and demo videos for the project README.

## Screenshots

Place files in [`../screenshots/`](../screenshots/) using these names:

| File | Description |
|---|---|
| `00-demo-video-thumbnail.png` | Thumbnail for the demo video link in README |
| `01-splash.png` | Splash / launch screen |
| `02-auth-login.png` | Login & sign-up screen |
| `03-email-verification.png` | Email verification waiting screen |
| `04-social-login.png` | Google / GitHub sign-in options |
| `05-deep-link-verification.png` | App opened from verification email |
| `06-home.png` | Home / main screen after auth |

### Capture tips

- Use a **Pixel emulator** or physical device at **1080×2400** or similar
- Prefer **light theme** for consistency across the README gallery
- Export as **PNG** or **WebP** (keep each file under ~500 KB when possible)
- Hide debug overlays (Chucker notification, layout bounds) before capturing

## Videos

Place files in [`../videos/`](../videos/):

| File | Description |
|---|---|
| `atlasfly-demo.mp4` | 60–90 second walkthrough for recruiters |

### Recording tips

- Record at **1080p**, 30 fps
- Show: cold start → sign up → verify email → land on home
- Keep narration optional; on-screen flow should be self-explanatory
- For GitHub README embedding, consider uploading to **YouTube (unlisted)** and linking the thumbnail

## Quick checklist

- [ ] `01-splash.png`
- [ ] `02-auth-login.png`
- [ ] `03-email-verification.png`
- [ ] `04-social-login.png`
- [ ] `05-deep-link-verification.png`
- [ ] `06-home.png`
- [ ] `00-demo-video-thumbnail.png`
- [ ] `atlasfly-demo.mp4` (or hosted video URL in README)
