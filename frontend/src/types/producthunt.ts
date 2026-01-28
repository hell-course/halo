export interface ProductHuntPost {
  id: string;
  name: string;
  tagline: string;
  description: string;
  url: string; // Product Hunt URL
  websiteUrl: string; // Product's actual website URL
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
