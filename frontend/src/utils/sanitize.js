/**
 * Client-side defense-in-depth input sanitizer for form fields.
 * Strips HTML tags, script payloads, and control characters before API submission.
 */
export function sanitizeInput(value) {
  if (typeof value !== 'string') return '';
  return value
    // eslint-disable-next-line no-control-regex
    .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '')
    .replace(/<\s*script[^>]*>[\s\S]*?<\s*\/\s*script\s*>/gi, '')
    .replace(/<[^>]+>/g, '')
    .replace(/(javascript:|vbscript:|data:text\/html)/gi, '')
    .replace(/\bon\w+\s*=/gi, '')
    .trim();
}
