export function useImageUrl() {
  const config = useRuntimeConfig();

  return (image?: string | null, fallback = "") => {
    const value = image?.trim();

    if (!value) {
      return fallback;
    }

    if (/^[a-z][a-z\d+.-]*:/i.test(value) || value.startsWith("//")) {
      return value;
    }

    const baseURL = config.public.backendBaseUrl.replace(/\/$/, "");
    const normalizedPath = value.startsWith("/") ? value : `/${value}`;

    return `${baseURL}${normalizedPath}`;
  };
}
