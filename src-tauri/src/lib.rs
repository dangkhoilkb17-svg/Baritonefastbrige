use base64::{engine::general_purpose::STANDARD, Engine as _};
use image::{DynamicImage, RgbaImage};
use screenshots::Screen;
use serde::{Deserialize, Serialize};
use std::io::Cursor;

#[derive(Debug, Deserialize)]
#[serde(tag = "kind", rename_all = "lowercase")]
pub enum CaptureMode {
    Fullscreen,
    Region {
        x: i32,
        y: i32,
        width: u32,
        height: u32,
    },
}

#[derive(Debug, Serialize)]
pub struct CaptureResult {
    pub data_url: String,
    pub width: u32,
    pub height: u32,
}

#[derive(Debug, PartialEq)]
struct Region {
    x: u32,
    y: u32,
    width: u32,
    height: u32,
}

fn validate_region(x: i32, y: i32, width: u32, height: u32) -> Result<Region, String> {
    if x < 0 || y < 0 {
        return Err("Region coordinates cannot be negative".into());
    }
    if width == 0 || height == 0 {
        return Err("Region width and height must be greater than zero".into());
    }
    Ok(Region {
        x: x as u32,
        y: y as u32,
        width,
        height,
    })
}

#[tauri::command]
fn capture_screenshot(mode: CaptureMode) -> Result<CaptureResult, String> {
    let screen = Screen::all()
        .map_err(|error| format!("Unable to enumerate displays: {error}"))?
        .into_iter()
        .next()
        .ok_or_else(|| "No display is available".to_string())?;
    let image = screen
        .capture()
        .map_err(|error| format!("Unable to capture display: {error}"))?;
    let (rgba, width, height) = match mode {
        CaptureMode::Fullscreen => {
            let width = image.width();
            let height = image.height();
            let rgba = RgbaImage::from_raw(width, height, image.as_raw().to_vec())
                .ok_or_else(|| "Captured display had an invalid pixel buffer".to_string())?;
            (rgba, width, height)
        }
        CaptureMode::Region {
            x,
            y,
            width,
            height,
        } => {
            let region = validate_region(x, y, width, height)?;
            if region.x + region.width > image.width() || region.y + region.height > image.height()
            {
                return Err("Region must fit within the captured display".into());
            }
            let source =
                RgbaImage::from_raw(image.width(), image.height(), image.as_raw().to_vec())
                    .ok_or_else(|| "Captured display had an invalid pixel buffer".to_string())?;
            let cropped =
                image::imageops::crop_imm(&source, region.x, region.y, region.width, region.height)
                    .to_image();
            let width = cropped.width();
            let height = cropped.height();
            (cropped, width, height)
        }
    };

    let mut png = Cursor::new(Vec::new());
    DynamicImage::ImageRgba8(rgba)
        .write_to(&mut png, image::ImageFormat::Png)
        .map_err(|error| format!("Unable to encode screenshot: {error}"))?;
    Ok(CaptureResult {
        data_url: format!(
            "data:image/png;base64,{}",
            STANDARD.encode(png.into_inner())
        ),
        width,
        height,
    })
}

pub fn run() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![capture_screenshot])
        .run(tauri::generate_context!())
        .expect("error while running screenshot MVP");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn accepts_positive_region() {
        assert_eq!(
            validate_region(10, 20, 300, 200).unwrap(),
            Region {
                x: 10,
                y: 20,
                width: 300,
                height: 200
            }
        );
    }

    #[test]
    fn rejects_negative_coordinates_and_empty_dimensions() {
        assert!(validate_region(-1, 0, 10, 10).is_err());
        assert!(validate_region(0, 0, 0, 10).is_err());
        assert!(validate_region(0, 0, 10, 0).is_err());
    }
}
