import type { NextConfig } from "next";

// Las pantallas son de cliente y le piden todo a la API de Spring,
// asi que no usamos Cache Components (cada vez que se entra a una pantalla arranca limpia).
const nextConfig: NextConfig = {
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
