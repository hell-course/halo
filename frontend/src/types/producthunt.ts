export interface ProductHuntPost {
  id: string;
  name: string;
  tagline: string;
  description: string;
  url: string; // Product Hunt URL
  website?: string; // Product's actual website URL
  websiteUrl?: string; // Backward compatibility for older payloads
  votesCount: number;
  createdAt: string; // ISO 8601 format
  similarity: number; // For semantic similarity score

  thumbnail?: { // Optional as it might be null
    url: string;
  };
  topics?: { // Optional as it might be null
    edges: Array<{
      node: {
        name: string;
      };
    }>;
  };
}
